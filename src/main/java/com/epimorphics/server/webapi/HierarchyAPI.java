/******************************************************************
 * File:        HierarchyAPI.java
 * Created by:  Dave Reynolds
 * Created on:  9 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi;

import javax.ws.rs.core.MultivaluedMap;

import com.epimorphics.server.webapi.dsapi.ResourceCache;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
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
    
}
