/******************************************************************
 * File:        ResourceList.java
 * Created by:  Dave Reynolds
 * Created on:  9 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.epimorphics.server.core.Store;
import com.epimorphics.server.general.PrefixService;
import com.epimorphics.server.webapi.DSAPIManager;
import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Utility which issues a SPARQL query and returns a json serializable
 * list of the matching resources. The query will be expanded using the
 * global prefix service. Caches the results in memory to avoid keeping
 * the store locked - scaling issue.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResourceList implements JSONWritable {
    protected List<Value> matches;

    public ResourceList(String query, String var, DSAPIManager man) {
        this(query, var, man, null);
    }
    
    public ResourceList(String query, String var, Store store, String apiBase) {
        init(query, var, store, apiBase);
    }
    
    private void init(String query, String var, Store store, String apiBase) {
        matches = new ArrayList<>();
        String q = PrefixUtils.expandQuery(query, PrefixService.get().getPrefixes());
        ResourceCache rc = ResourceCache.get();
        QueryExecution qexec = QueryExecutionFactory.create(q, store.getUnionModel());
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.next();
                RDFNode n = soln.get(var);
                if (n != null) {
                    Value v = rc.valueFromNode( n );
                    if (apiBase != null && v instanceof ResourceValue) {
                        ((ResourceValue)v).setApiBase(apiBase);
                    }
                    matches.add( v );
                }
            }
        } finally {
            qexec.close();
        }
    }
    
    public ResourceList(String query, String var, DSAPIManager man, String apiBase) {
        Store store = man.getStore();
        store.lock();
        try {
            init(query, var, store, apiBase);
        } finally {
            store.unlock();
        }
    }

    public ResourceList(List<Resource> resources) {
        ResourceCache rc = ResourceCache.get();
        matches = new ArrayList<>(resources.size());
        for (Resource r : resources) {
            matches.add( rc.valueFromResource(r) );
        }
    }

    public ResourceList(List<Resource> resources, String apiBase) {
        ResourceCache rc = ResourceCache.get();
        matches = new ArrayList<>(resources.size());
        for (Resource r : resources) {
            ResourceValue v = rc.valueFromResource(r);
            v.setApiBase(apiBase);
            matches.add( v );
        }
    }
    
    
    public void setMatches(List<Value> matches) {
        this.matches = matches;
    }

    public List<Value> getMatches() {
        return matches;
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startArray();
        for (Iterator<Value> i = matches.iterator(); i.hasNext();) {
            i.next().writeTo(out);
            if (i.hasNext()) {
                out.arraySep();
            }
        }
        out.finishArray();
    }
    
    
}
