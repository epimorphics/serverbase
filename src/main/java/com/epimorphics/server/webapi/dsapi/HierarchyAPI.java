/******************************************************************
 * File:        HierarchyAPI.java
 * Created by:  Dave Reynolds
 * Created on:  9 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.server.core.Store;
import com.epimorphics.server.indexers.LuceneIndex;
import com.epimorphics.server.indexers.LuceneResult;
import com.epimorphics.server.webapi.DSAPIManager;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Resource (in the RESTish sense) to represent a hierarchical code list.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class HierarchyAPI {
    public static final String ROOTS_PARAM = "_roots";
    public static final String COLLECTIONS_PARAM = "_collections";
    public static final String LEVELS_PARAM = "_levels";
    public static final String CHILDREN_PARAM = "_below";
    public static final String TEXT_PARAM = "_text";
    
    protected DSAPIManager man;
    protected Resource codelist;
    
    public HierarchyAPI(String id, DSAPIManager man) {
        this.man = man;
        codelist = ResourceCache.get().resourceFromID(id, null);
    }
    
    public JSONWritable handleRequest(MultivaluedMap<String, String> params) {
        if (params.containsKey(COLLECTIONS_PARAM)) {
            return listCollections();
        } else if (params.containsKey(LEVELS_PARAM)) {
            return listLevels();
        } else if (params.containsKey(CHILDREN_PARAM)) {
            return listChildren( params.getFirst(CHILDREN_PARAM) );
        } else if (params.containsKey(TEXT_PARAM)) {
            return textSearch( params.getFirst(TEXT_PARAM) );
        } else {
            return listRoots();
        }
    }
    
    protected JSONWritable listRoots() {
        String query = String.format("SELECT ?x WHERE {<%s> skos:hasTopConcept ?x. }", codelist.getURI());
        // TODO generalize to UNION query that allows links the other way up
        return new ResourceList(query, "x", man);
    }
    
    protected JSONWritable listCollections() {
        // TODO
        return null;
    }
    
    protected JSONWritable listLevels() {
        // TODO
        return null;
    }
    
    protected JSONWritable listChildren(String parent) {
        String query = String.format("SELECT ?x WHERE {?x skos:broader <%s>. }", ResourceCache.get().valueFromID(parent).getUri());
        // TODO generalize to UNION query that allows links the other way up
        return new ResourceList(query, "x", man);
    }
    
    protected JSONWritable textSearch(String query) {
        // TODO rewrite to use jena-text
        // TODO remove dependency on magic indexer name
        LuceneIndex index = ServiceConfig.get().getServiceAs("index", LuceneIndex.class);
        if (index == null) {
            throw new EpiException("No index configured for text search");
        }
        LuceneResult[] matches = index.search(query, 0, 1000);
        Store store = man.getStore();
        store.lock();
        try {
            List<Resource> codes = new ArrayList<>();
            Model model = store.getUnionModel();
            for (LuceneResult lr : matches) {
                Resource r = model.getResource( lr.getURI() );
                if (r.hasProperty(SKOS.inScheme, codelist)) {
                    codes.add(r);
                }
            }
            return new ResourceList(codes);
        } finally {
            store.unlock();
        }
    }

}
