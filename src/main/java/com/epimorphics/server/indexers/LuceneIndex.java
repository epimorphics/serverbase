/******************************************************************
 * File:        LuceneIndex.java
 * Created by:  Dave Reynolds
 * Created on:  1 Dec 2012
 * 
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.indexers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.epimorphics.server.core.Indexer;
import com.epimorphics.server.core.Service;
import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.FileUtil;
import com.epimorphics.vocabs.Li;
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
 * formated in Turtle type notation 'lit' or 'lit'^^type or 'lit'@lang and indexed as non-shared strings.
 * </p>
 * <p>
 * Configuration parameters are:
 * <ul>
 *  <li>location - directory where the index should be built and stored</li>
 *  <li>config  - RDF file giving the index configuration</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO implement the faceted search indexing support

public class LuceneIndex implements Indexer, Service {
    public static final String LOCATION_PARAM = "location";
    public static final String CONFIG_PARAM = "config";

    public static final String FIELD_URI = "uri";
    public static final String FIELD_GRAPH = "graph";
    
    protected Map<String, String> config;
    protected String indexLocation;
    protected boolean indexAll;
    protected Set<Resource> labelProps = new HashSet<Resource>();
    protected Set<Resource> labelOnlyProps = new HashSet<Resource>();
    protected Set<Resource> ignoreProps = new HashSet<Resource>();
    protected Set<Resource> valueProps = new HashSet<Resource>();
    
    @Override
    public void init(Map<String, String> config) {
        this.config = config;
    
        indexLocation = getRequiredFileParam(LOCATION_PARAM);
        FileUtil.ensureDir(indexLocation);
        
        String configLocation = getRequiredFileParam(CONFIG_PARAM);
        Model configModel = FileManager.get().loadModel(configLocation);
        analyseConfigModel( configModel );
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

    private String getRequiredFileParam(String param) {
        String location = config.get(param);
        if (location == null) {
            throw new EpiException("Missing requried configuration parameter: " + param);
        }
        return ServiceConfig.get().expandFileLocation(location);
    }
    
    @Override
    public void postInit() {
    }

    @Override
    public void addGraph(String graphname, Model graph) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateGraph(String graphname, Model graph) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteGraph(String graphname) {
        // TODO Auto-generated method stub
        
    }

}
