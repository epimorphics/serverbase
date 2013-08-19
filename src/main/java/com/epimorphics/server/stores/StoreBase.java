/******************************************************************
 * File:        StoreBase.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *****************************************************************/

package com.epimorphics.server.stores;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.jena.fuseki.server.DatasetRef;
import org.apache.jena.fuseki.server.DatasetRegistry;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.server.core.Indexer;
import com.epimorphics.server.core.Mutator;
import com.epimorphics.server.core.Service;
import com.epimorphics.server.core.ServiceBase;
import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.server.core.Store;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.FileUtil;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Base implementation of a generic store. Supports  linking to indexer and mutator services.
 * Supports optional logging of all requests to a nominated file system.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class StoreBase extends ServiceBase implements Store, Service {
    static Logger log = LoggerFactory.getLogger(StoreBase.class);

    public static final String INDEXER_PARAM = "indexer";
    public static final String MUTATOR_PARAM = "mutator";
    public static final String JENA_TEXT_PARAM = "jena-text";
    public static final String LOG_PARAM = "log";

    public static final String ADD_ACTION = "ADD";
    public static final String UPDATE_ACTION = "UPDATE";
    public static final String DELETE_ACTION = "DELETE";
    
    public static final String QUERY_ENDPOINT_PARAM    = "ep";

    protected Dataset dataset;
    protected volatile List<Indexer> indexers = new ArrayList<Indexer>();
    protected volatile List<Mutator> mutators = new ArrayList<Mutator>();
    protected String logDirectory;
    protected boolean inWrite = false;

    @Override
    public void init(Map<String, String> config, ServletContext context) {
        super.init(config, context);
        logDirectory = config.get(LOG_PARAM);
        if (logDirectory != null) {
            logDirectory = ServiceConfig.get().expandFileLocation(logDirectory);
            FileUtil.ensureDir(logDirectory);
        }
    }

    /**
     * Install a jena-text dataset wrapper round this store.
     * This is an alternative to the indexer system.
     */
    protected void installJenaText() {
        if (config.containsKey(JENA_TEXT_PARAM)) {
            String dirname = getRequiredFileParam(JENA_TEXT_PARAM);
            Directory dir = null;
            if (dirname.equals("mem")) {
                dir =  new RAMDirectory();
            } else {
                try {
                    File dirf = new File(dirname);
                    dir = FSDirectory.open(dirf);
                } catch (IOException e) {
                    throw new EpiException("Failed to create jena-text lucence index area", e);
                }
            }
            EntityDefinition entDef = new EntityDefinition("uri", "text", RDFS.label.asNode()) ;
            dataset = TextDatasetFactory.createLucene(dataset, dir, entDef) ;
        }
    }
    
    /**
     * Configure a Fuseki query servlet for this store. The
     * web.xml file needs to map the servlet to a matching context path.
     */
    protected void installQueryEndpoint( ServletContext context) {
        String qEndpoint = config.get(QUERY_ENDPOINT_PARAM);
        if (qEndpoint != null) {
            String base = context.getContextPath();
            if ( ! base.endsWith("/")) {
                base += "/";
            }
            base += qEndpoint;
            DatasetRef ds = new DatasetRef();
            ds.name = qEndpoint;
            ds.query.endpoints.add("query" ); 
            ds.init();
            ds.dataset = dataset.asDatasetGraph();
            DatasetRegistry.get().put(base, ds);
            log.info("Installing SPARQL query endpoint at " + base + "/query");
        }
    }
    
    @Override
    public void postInit() {
        String indexerNames = config.get(INDEXER_PARAM);
        if (indexerNames != null) {
            for (String indexerName : indexerNames.split(";")) {
                Service indexer = ServiceConfig.get().getService(indexerName);
                if (indexer instanceof Indexer) {
                    indexers.add( (Indexer) indexer );
                    log.info("Configured indexer for store");
                } else {
                    throw new EpiException("Configured indexer doesn't seem to be an Indexer: " + indexerName);
                }
            }
        }
        String mutatorNames = config.get(MUTATOR_PARAM);
        if (mutatorNames != null) {
            for (String name : mutatorNames.split(";")) {
                Service mutator = ServiceConfig.get().getService(name);
                if (mutator instanceof Mutator) {
                    mutators.add( (Mutator) mutator );
                } else {
                    throw new EpiException("Configured mutator doesn't seem to be a Mutator: " + name);
                }
            }
        }
    }

    protected abstract void doAddGraph(String graphname, Model graph);
    protected abstract void doAddGraph(String graphname, InputStream input, String mimeType);
    protected abstract void doDeleteGraph(String graphname);

    @Override
    abstract public Dataset asDataset();

    @Override
    public void addGraph(String graphname, Model graph) {
        logAction(ADD_ACTION, graphname, graph);
        mutate(graph);
        index(graphname, graph, false);
        doAddGraph(graphname, graph);
    }

    @Override
    public void updateGraph(String graphname, Model graph) {
        logAction(UPDATE_ACTION, graphname, graph);
        doDeleteGraph(graphname);
        mutate(graph);
        index(graphname, graph, true);
        doAddGraph(graphname, graph);
    }

    @Override
    public void deleteGraph(String graphname) {
        logAction(DELETE_ACTION, graphname, null);
        for (Indexer i : indexers) {
            i.deleteGraph(graphname);
        }
        doDeleteGraph(graphname);
    }

    @Override
    public void addGraph(String graphname, InputStream input, String mimeType) {
        doAddGraph(graphname, input, mimeType);
        logNamed(ADD_ACTION, graphname);
        mutateNamed(graphname);
        indexNamed(graphname, false);
    }

    @Override
    public void updateGraph(String graphname, InputStream input, String mimeType) {
        doDeleteGraph(graphname);
        doAddGraph(graphname, input, mimeType);
        logNamed(UPDATE_ACTION, graphname);
        mutateNamed(graphname);
        indexNamed(graphname, true);
    }

    @Override
    synchronized public void addIndexer(Indexer indexer) {
        List<Indexer> newIndexes = new ArrayList<Indexer>( indexers );
        newIndexes.add(indexer);
        // This should be an atomics switch of lists so no need to sync the methods that use the list
        indexers = newIndexes;
    }

    @Override
    synchronized public void addMutator(Mutator mutator) {
        List<Mutator> newl= new ArrayList<Mutator>( mutators );
        newl.add(mutator);
        // This should be an atomics switch of lists so no need to sync the methods that use the list
        mutators = newl;
    }

    /** Lock the dataset for reading */
    public synchronized void lock() {
        Dataset dataset = asDataset();
        if (dataset.supportsTransactions()) {
            dataset.begin(ReadWrite.READ);
        } else {
            dataset.asDatasetGraph().getLock().enterCriticalSection(Lock.READ);
        }
    }

    /** Lock the dataset for write */
    public synchronized void lockWrite() {
        Dataset dataset = asDataset();
        if (dataset.supportsTransactions()) {
            dataset.begin(ReadWrite.WRITE);
            inWrite = true;
        } else {
            dataset.asDatasetGraph().getLock().enterCriticalSection(Lock.WRITE);
        }
    }

    /** Unlock the dataset */
    public synchronized void unlock() {
        Dataset dataset = asDataset();
        if (dataset.supportsTransactions()) {
            if (inWrite) {
                dataset.commit();
                inWrite = false;
            }
            dataset.end();
        } else {
            dataset.asDatasetGraph().getLock().leaveCriticalSection();
        }
    }

    /** Unlock the dataset, aborting the transaction. Only useful if the dataset is transactional */
    public synchronized void abort() {
        Dataset dataset = asDataset();
        if (dataset.supportsTransactions()) {
            if (inWrite) {
                dataset.abort();
                inWrite = false;
            }
            dataset.end();
        } else {
            dataset.asDatasetGraph().getLock().leaveCriticalSection();
        }
    }

    // Internal methods

    protected void mutate(Model graph) {
        for (Mutator mutator : mutators) {
            mutator.mutate(graph);
        }
    }

    protected void mutateNamed(String graphname) {
        if (!mutators.isEmpty()) {
            lockWrite();
            try {
                mutate( asDataset().getNamedModel(graphname) );
            } finally {
                unlock();
            }
        }
    }

    protected void index(String graphname, Model graph, boolean update) {
        for (Indexer i : indexers) {
            if (update) {
                i.updateGraph(graphname, graph);
            } else {
                i.addGraph(graphname, graph);
            }
        }
    }

    protected void indexNamed(String graphname, boolean update) {
        lock();
        try {
            index( graphname, asDataset().getNamedModel(graphname), update );
        } finally {
            unlock();
        }
    }

    protected void logAction(String action, String graph, Model data) {
        if (logDirectory != null) {
            String dir = logDirectory + File.separator + NameUtils.encodeSafeName(graph);
            FileUtil.ensureDir(dir);
            String filename = String.format("on-%s-%s.ttl", System.currentTimeMillis(), action);
            File logFile = new File(dir, filename);
            try {
                if (data != null) {
                    OutputStream out = new FileOutputStream(logFile);
                    data.write(out, FileUtils.langTurtle);
                    out.close();
                } else {
                    logFile.createNewFile();
                }
            } catch (IOException e) {
                log.error("Failed to create log file: " + logFile);
            }
        }
    }

    protected void logNamed(String action, String graphname) {
        if (logDirectory != null) {
            lockWrite();
            try {
                logAction(action, graphname, asDataset().getNamedModel(graphname) );
            } finally {
                unlock();
            }
        }
    }

}
