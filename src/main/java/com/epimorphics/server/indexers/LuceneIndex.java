/******************************************************************
 * File:        LuceneIndex.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.indexers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.server.core.Indexer;
import com.epimorphics.server.core.Service;
import com.epimorphics.server.core.ServiceBase;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.FileUtil;
import com.epimorphics.vocabs.Li;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Text index for entities in a store.
 * <p>
 * An entity is any resource in the graph that has one or more explicit types.
 * </p>
 * <p>
 * There are three types of index fields supported:
 * <ul>
 *  <li>label fields - which are indexed as free text (i.e. analyzed)</li>
 *  <li>value fields - which are indexed as node values</li>
 *  <li>facet fields - which are indexed to support faceted search</li>
 * </ul>
 * Node values are either resources (uses shared ByteRef of the URI), numbers (any
 * numeric literal which fits in a long is indexed as a Long) or literal (which are
 * indexed as non-shared lexical forms).
 * </p>
 * <p>
 * Configuration parameters are:
 * <ul>
 *  <li>location - directory where the index should be built and stored</li>
 *  <li>config  - RDF file giving the index configuration</li>
 *  <li>commitWindow - time in sec to wait after a change before committing, default 0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO implement the faceted search indexing support

// TODO do we need to periodically close the writer? Makes it hard to use NRT search.

public class LuceneIndex extends ServiceBase implements Indexer, Service {
    static Logger log = LoggerFactory.getLogger(Indexer.class);

    public static final String LOCATION_PARAM = "location";
    public static final String CONFIG_PARAM = "config";
    public static final String COMMIT_PARAM = "commitWindow";

    public static final String FIELD_URI = "uri";
    public static final String FIELD_GRAPH = "graph";
    public static final String FIELD_LABEL = "label";

    protected static int DEFAULT_COMMIT_WINDOW = 0;

    protected boolean indexAll;
    protected Set<Resource> labelProps = new HashSet<Resource>();
    protected Set<Resource> labelOnlyProps = new HashSet<Resource>();
    protected Set<Resource> ignoreProps = new HashSet<Resource>();
    protected Set<Resource> valueProps = new HashSet<Resource>();

    protected Directory indexDir;

    protected static IndexWriter writer;
    protected static SearcherManager searchManager;

    protected int batchDepth = 0;
    protected int commitWindow = DEFAULT_COMMIT_WINDOW;
    protected boolean commitScheduled = false;
    protected static Timer cleanupTimer;

    @Override
    public void init(Map<String, String> config, ServletContext context) {
        super.init(config, context);
        try {
            String indexLocation = config.get(LOCATION_PARAM);
            if (indexLocation == null) {
                // Typically used for testing only
                log.warn("No index location, creating RAM directory");
                indexDir = new RAMDirectory();
                getIndexWriter().commit();
            } else {
                File indexF = new File(indexLocation);
                indexDir = FSDirectory.open( indexF );
                if (!indexF.exists() || (indexF.isDirectory() && indexF.list().length == 0)) {
                    log.warn("No existing index files, initializing directory " + indexLocation);
                    FileUtil.ensureDir(indexLocation);
                    getIndexWriter().commit();
                }
            }

            String commit = config.get(COMMIT_PARAM);
            if (commit != null) {
                try {
                    commitWindow = Integer.parseInt(commit) * 1000;
                } catch (NumberFormatException e) {
                    log.error("Bad format for commit window config parameter: " + commit + ", using default");
                }
            }

            IndexWriter writer = getIndexWriter();
            searchManager = new SearcherManager(writer, true, null);

            String configLocation = getRequiredFileParam(CONFIG_PARAM);
            Model configModel = FileManager.get().loadModel(configLocation);
            analyseConfigModel( configModel );
        } catch (Exception e) {
            throw new EpiException(e);
        }
    }

    @Override
    public void addGraph(String graphname, Model graph) {
        indexGraph(graphname, graph, false);
    }

    @Override
    public void updateGraph(String graphname, Model graph) {
        indexGraph(graphname, graph, true);
    }

    @Override
    public void deleteGraph(String graphname) {
        try {
            IndexWriter iwriter = getIndexWriter();
            iwriter.deleteDocuments(new Term(FIELD_GRAPH, graphname));
            requestCommit();
        } catch (Exception e) {
            throw new EpiException(e);
        }
    }

    /**
     * Search the index for entities which match a lucene query. Use field "label" for
     * searching on lables (e.g. PhraseQuery or TermQuery).
     */
    public LuceneResult[] search(Query query, int offset, int maxResults) {
        try {
            IndexSearcher searcher = searchManager.acquire();
            try {
                int searchLimit = offset + maxResults;
                TopDocs matches = searcher.search(query, searchLimit);
                ScoreDoc[] hits = matches.scoreDocs;
                LuceneResult[] results = new LuceneResult[hits.length - offset];
                for (int i = offset; i < hits.length; i++) {
                    ScoreDoc hit = hits[i];
                    results[i-offset] = new LuceneResult(searcher.getIndexReader()
                            .document(hit.doc), hit.score);
                }
                return results;
            } finally {
                searchManager.release(searcher);
            }
        } catch (IOException e) {
            throw new EpiException(e);
        }
    }

    /**
     * Search the index for entities which match a lucene query using the standard lucene
     * <a href="http://lucene.apache.org/core/4_0_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package_description">synta</a>.
     * Fields names indexed from the RDF will be URIs and so characters like ':' and '/' need to be escaped in the query string.
     */
    public LuceneResult[] search(String query, int offset, int maxResults) {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
        QueryParser parser = new QueryParser(Version.LUCENE_40, FIELD_LABEL, analyzer);
        try {
            return search(parser.parse(query), offset, maxResults);
        } catch (ParseException e) {
            throw new EpiException(e);
        }
    }


    private void analyseConfigModel(Model configModel) {
        ResIterator ri = configModel.listResourcesWithProperty(RDF.type, Li.Config);
        if (ri.hasNext()) {
            Resource configR = ri.next();

            Statement indexAllS = configR.getProperty(Li.indexAll);
            if (indexAllS != null && indexAllS.getObject().isLiteral()) {
                indexAll = indexAllS.getObject().asLiteral().getBoolean();
            }

            extractSet(configR, Li.ignoreProp, ignoreProps);
            extractSet(configR, Li.labelOnlyProp, labelOnlyProps);
            extractSet(configR, Li.labelProp, labelProps);
            extractSet(configR, Li.valueProp, valueProps);
        } else {
            throw new EpiException("Can't find root config resource for Lucene indexer");
        }
    }

    private void extractSet(Resource configR, Property p, Set<Resource> set) {
        StmtIterator si = configR.listProperties(p);
        while (si.hasNext()) {
            RDFNode n = si.next().getObject();
            if (n.isURIResource()) {
                set.add(n.asResource());
            }
        }
    }

    protected void indexGraph(String graphname, Model graph, boolean update) {
        try {
            IndexWriter iwriter = getIndexWriter();
            ResIterator ri = graph.listSubjectsWithProperty(RDF.type);
            while (ri.hasNext()) {
                indexEntity(iwriter, update, graphname, ri.next());
            }
            requestCommit();
        } catch (Exception e) {
            throw new EpiException(e);
        }
    }

    private void indexEntity(IndexWriter iwriter, boolean update, String graphname, Resource entity) throws IOException {
        if (entity.isAnon()) return;
        Document doc = new Document();
        doc.add( new StringField(FIELD_URI, entity.getURI(), Field.Store.YES) );
        doc.add( new StringField(FIELD_GRAPH, graphname, Field.Store.YES) );
        StmtIterator si = entity.listProperties();
        while (si.hasNext()) {
            Statement s = si.next();
            Property p = s.getPredicate();
            RDFNode value = s.getObject();
            String valueStr = asString(value);
            if (labelProps.contains(p)) {
                doc.add( new TextField(p.getURI(), valueStr, Field.Store.YES) );
                doc.add( new TextField(FIELD_LABEL, valueStr, Field.Store.NO) );
            } else if (labelOnlyProps.contains(p)) {
                doc.add( new TextField(p.getURI(), valueStr, Field.Store.NO) );
                doc.add( new TextField(FIELD_LABEL, valueStr, Field.Store.NO) );
            } else if (valueProps.contains(p) || (indexAll && !ignoreProps.contains(p))) {
                if (value.isURIResource()) {
                    doc.add( new StringField(p.getURI(), value.asResource().getURI(), Field.Store.YES) );
                    // Alternative below would share storage of URIs but only allows per document field
//                    doc.add( new DerefBytesDocValuesField(p.getURI(), new BytesRef(value.asResource().getURI())) );
                } else if (value.isLiteral()) {
                    Literal lvalue = value.asLiteral();
                    Object jvalue = lvalue.getValue();
                    if (jvalue instanceof Long || jvalue instanceof Integer) {
                        doc.add( new LongField(p.getURI(), ((Number)jvalue).longValue(), Field.Store.YES) );
                    } else {
                        doc.add( new TextField(p.getURI(), valueStr, Field.Store.YES) );
                    }
                }
            }
        }
        if (update) {
            iwriter.updateDocument(new Term(FIELD_URI, entity.getURI()), doc);
        } else {
            iwriter.addDocument(doc);
        }
    }

    private String asString(RDFNode n) {
        if (n.isLiteral()) {
            return n.asLiteral().getLexicalForm();
        } else if (n.isURIResource()) {
            return n.asResource().getURI();
        } else {
            return "[]";
        }
    }

    protected synchronized IndexWriter getIndexWriter() {
        if (writer == null) {
            try {
                Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
                IndexWriterConfig config = new IndexWriterConfig(
                        Version.LUCENE_40, analyzer);
                writer = new IndexWriter(indexDir, config);
                config.setOpenMode( OpenMode.CREATE_OR_APPEND );
            } catch (Exception e) {
                throw new EpiException(e);
            }
        }
        return writer;
    }

    @Override
    public synchronized void startBatch() {
        batchDepth++;
    }

    @Override
    public synchronized void endBatch() {
        if (batchDepth <= 0) {
            throw new EpiException("Attemted to end a non-existent index batch");
        }
        if (--batchDepth == 0) {
            scheduleCommit();
        }
    }

    protected void requestCommit() throws IOException {
        synchronized (this) {
            if (batchDepth <= 0) {
                scheduleCommit();
            }
        }
        searchManager.maybeRefresh();
    }

    protected void scheduleCommit() {
        if (commitWindow == 0) {
            doCommit();
        } else {
            synchronized (this) {
                if (!commitScheduled) {
                    if (cleanupTimer == null) {
                        cleanupTimer = new Timer(true);
                    }
                    cleanupTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            commitScheduled = false;
                            doCommit();
                        }
                    }, commitWindow);
                    commitScheduled = true;
                }
            }
        }
    }

    protected void doCommit() {
        IndexWriter writer = getIndexWriter();
        try {
            writer.commit();
            log.debug("Lucene index committed");
        } catch (Exception e) {
            // try to save the data
            try {
                writer.close();
            } catch (IOException e1) {
                // Do nothing, we've already taken our best shot
                log.error("Failed to even close index writer after commit error", e);
            }
            throw new EpiException(e);
        }
    }

}
