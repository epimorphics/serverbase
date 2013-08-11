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
        matches = new ArrayList<>();
        String q = PrefixUtils.expandQuery(query, PrefixService.get().getPrefixes());
        Store store = man.getStore();
        ResourceCache rc = ResourceCache.get();
        store.lock();
        try {
            QueryExecution qexec = QueryExecutionFactory.create(q, store.getUnionModel());
            try {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.next();
                    RDFNode n = soln.get(var);
                    if (n != null) {
                        matches.add( rc.valueFromNode( n ) );
                    }
                }
            } finally {
                qexec.close();
            }
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
