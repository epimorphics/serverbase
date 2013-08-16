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
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.server.core.Store;
import com.epimorphics.server.indexers.LuceneIndex;
import com.epimorphics.server.indexers.LuceneResult;
import com.epimorphics.server.webapi.DSAPIManager;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.epimorphics.server.webapi.marshalling.JSONWritableObject;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.Dsapi;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Resource (in the RESTish sense) to represent a hierarchical code list.
 * Relies on prefix configuration including at least skos:, xkos:, rdf:
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class HierarchyAPI {
    public static final String ROOTS_PARAM = "_roots";
    public static final String COLLECTIONS_PARAM = "_collections";
    public static final String LEVELS_PARAM = "_levels";
    public static final String TERMS_PARAM = "_terms";
    public static final String BELOW_PARAM = "_below";
    public static final String TEXT_PARAM = "_text";
    public static final String CHILDREN_PARAM = "_children";
    public static final String MEMBER_PARAM = "_members";
    public static final String PARENT_PARAM = "_parent";
    public static final String PATH_PARAM = "_path";
    
    protected DSAPIManager man;
    protected ResourceValue resource;
    
    
    public HierarchyAPI(String id, DSAPIManager man) {
        this.man = man;
        resource = ResourceCache.get().valueFromID(id);
    }
    
    public JSONWritable handleCodelistRequest(MultivaluedMap<String, String> params) {
        JSONWritableObject result = new JSONWritableObject();
        addResourceDescription(result, resource);
        result.put(JSONConstants.API, man.getApiBase() + "/codelist/" + resource.getId());
        if (params.containsKey(COLLECTIONS_PARAM)) {
            result.put(COLLECTIONS_PARAM, listCollections());
        }
        if (params.containsKey(LEVELS_PARAM)) {
            result.put(LEVELS_PARAM, listLevels());
        } 
        if (params.containsKey(BELOW_PARAM)) {
            ResourceList terms = null;
            for (String parent : params.get(BELOW_PARAM)) {
                if (terms == null) {
                    terms = listChildren(parent);
                } else {
                    terms.getMatches().addAll( listChildren(parent).getMatches() );
                }
            }
            result.put(TERMS_PARAM, terms );
        } 
        if (params.containsKey(TEXT_PARAM)) {
            result.put(TERMS_PARAM, textSearch( params.getFirst(TEXT_PARAM) ) );
        }
        if (params.containsKey(ROOTS_PARAM)) {
            result.put(ROOTS_PARAM, listRoots());
        }
        return result;
    }
    
    private void addResourceDescription(JSONWritableObject object, ResourceValue rv) {
        object.put(JSONConstants.ID, rv.getId());
        object.put(JSONConstants.URI, rv.getUri());
        object.put(JSONConstants.LABEL, rv.getLabel());
    }
    
    protected JSONWritable listRoots() {
        String query = String.format("SELECT ?x WHERE {<%s> skos:hasTopConcept ?x. }", resource.getUri());
        // TODO generalize to UNION query that allows links the other way up
        return new ResourceList(query, "x", man, man.getApiBase() + "/code/");
    }
    
    protected JSONWritable listCollections() {
        String query = String.format("SELECT ?x WHERE {<%s> <%s> ?x. }", resource.getUri(),  Dsapi.collection);
        return new ResourceList(query, "x", man, man.getApiBase() + "/collection/");
    }
    
    protected JSONWritable listLevels() {
        String query = String.format("SELECT ?x WHERE {<%s> xkos:levels / rdf:rest * / rdf:first ?x. ?x xkos:level ?l.} ORDER BY ?l", 
                resource.getUri());
        return new ResourceList(query, "x", man, man.getApiBase() + "/collection/");
    }
    
    protected ResourceList listChildren(String parent) {
        String query = String.format("SELECT ?x WHERE {?x skos:broader <%s>. }", ResourceCache.get().valueFromID(parent).getUri());
        // TODO generalize to UNION query that allows links the other way up
        return new ResourceList(query, "x", man, man.getApiBase() + "/code/");
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
            Resource codelist = model.getResource( resource.getUri() );
            for (LuceneResult lr : matches) {
                Resource r = model.getResource( lr.getURI() );
                if (r.hasProperty(SKOS.inScheme, codelist)) {
                    codes.add(r);
                }
            }
            return new ResourceList(codes, man.getApiBase() + "/code/");
        } finally {
            store.unlock();
        }
    }
    
    public JSONWritable handleCollectionRequest(MultivaluedMap<String, String> params) {
        JSONWritableObject result = new JSONWritableObject();
        addResourceDescription(result, resource);
        result.put(JSONConstants.API, man.getApiBase() + "/collection/" + resource.getId());
        if (params.containsKey(MEMBER_PARAM)) {
            String query = String.format("SELECT ?x WHERE {<%s> skos:member ?x. ?x a skos:Concept . }", resource.getUri());
            result.put(MEMBER_PARAM, new ResourceList(query, "x", man, man.getApiBase() + "/code/"));
        }
        if (params.containsKey(CHILDREN_PARAM)) {
            String query = String.format("SELECT ?x WHERE {<%s> skos:member ?x. ?x a skos:Collection . }", resource.getUri());
            result.put(CHILDREN_PARAM, new ResourceList(query, "x", man, man.getApiBase() + "/collection/"));
        }
        return result;
    }
    
    public JSONWritable handleCodeRequest(MultivaluedMap<String, String> params) {
        JSONWritableObject result = new JSONWritableObject();
        addResourceDescription(result, resource);
        result.put(JSONConstants.API, man.getApiBase() + "/code/" + resource.getId());
        if (params.containsKey(CHILDREN_PARAM)) {
//        if (true) {
            String query = String.format("SELECT ?x WHERE {?x skos:broader <%s>. }", resource.getUri());
            // TODO generalize to UNION query that allows links the other way up
            result.put(CHILDREN_PARAM, new ResourceList(query, "x", man, man.getApiBase() + "/code/"));
        }
        if (params.containsKey(PARENT_PARAM)) {
//        if (true) {
            String query = String.format("SELECT ?x WHERE { <%s> skos:broader ?x. }", resource.getUri());
            // TODO generalize to UNION query that allows links the other way up
            result.put(PARENT_PARAM, new ResourceList(query, "x", man, man.getApiBase() + "/code/"));
        }
        if (params.containsKey(PATH_PARAM)) {
            Store store = man.getStore();
            store.lock();
            try {
                List<JSONWritable> path = new ArrayList<>();
                Model model = store.getUnionModel();
                Resource term = model.getResource( resource.getUri() );
                Resource parent = term.getPropertyResourceValue(SKOS.broader);
                do {
                    path.add( describeTerm(parent, store) );
                    parent = parent.getPropertyResourceValue(SKOS.broader);
                } while(parent != null);
                Collections.reverse(path);
                result.put(PATH_PARAM, path);
            } finally {
                store.unlock();
            }
        }
        return result;
    }
    

    protected JSONWritableObject describeTerm(Resource term, Store store) {
        JSONWritableObject description = new JSONWritableObject();
        addResourceDescription(description, ResourceCache.get().valueFromResource(term));
        String query = String.format("SELECT ?x WHERE {?x skos:broader <%s>. }", term.getURI());
        // TODO generalize to UNION query that allows links the other way up
        description.put(CHILDREN_PARAM, new ResourceList(query, "x", store, man.getApiBase() + "/code/"));
        return description;
    }

}
