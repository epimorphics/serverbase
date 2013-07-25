/******************************************************************
 * File:        Datatable.java
 * Created by:  Dave Reynolds
 * Created on:  24 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.collections.map.LRUMap;

import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.server.core.Store;
import com.epimorphics.server.webapi.impl.ArrayDatatable;
import com.epimorphics.server.webapi.impl.DatatableProjection;
import com.epimorphics.server.webapi.impl.DatatableResponse;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

@Path("system/tables")
public class DatatableAPI extends BaseEndpoint {
    protected static final String PARAM_query   = "query";
    protected static final String PARAM_columns = "cols";
    protected static final String PARAM_store   = "store";
    
    static LRUMap tableCache = new LRUMap(50);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DatatableResponse projectTable() {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        
        String query = getSafeParam(PARAM_query);
        String columns = getSafeParam(PARAM_columns);
        String storename = getSafeParam(PARAM_store);

        ArrayDatatable table = null;
        synchronized (tableCache) {
            table = (ArrayDatatable) tableCache.get(query);
        }
        if (table == null) {
            table = initializeTable(storename, query, columns);
            synchronized (tableCache) {
                tableCache.put(query, table);
            }
        }
        
        DatatableProjection projection = new DatatableProjection( flatten(parameters) );

        return table.project(projection);
    }
    
    private String getSafeParam(String name) {
        String val = uriInfo.getQueryParameters().getFirst(name);
        if (val == null || val.isEmpty()) {
            throw new EpiException("Missing required parameter: " + name);
        }
        return val;
    }
    
    private ArrayDatatable initializeTable(String storename, String query, String columns) {
        String[] varnames = columns.split(",");
        for (int i = 0; i < varnames.length; i++) {
            varnames[i] = varnames[i].trim();
        }
        
        Store store = ServiceConfig.get().getServiceAs(storename, Store.class);
        if (store == null) {
            throw new EpiException("Can't find requested store: " + storename);
        }
        
        Model m = store.getUnionModel();
        String q = PrefixUtils.expandQuery(query, m);
        QueryExecution qexec = QueryExecutionFactory.create(q, m);
        store.lock();
        ResultSetRewindable results = null;
        try {
            results = ResultSetFactory.copyResults(qexec.execSelect());
        } finally {
            qexec.close();
            store.unlock();
        }
        Object[][] data = new Object[results.size()][];
        int count = 0;
        while (results.hasNext()) {
            QuerySolution soln = results.next();
            Object[] row = new Object[varnames.length];
            for (int i = 0; i < varnames.length; i++) {
                RDFNode val = soln.get(varnames[i]);
                if (val.isLiteral()) {
                    row[i] = val.asLiteral().getValue();
                } else if (val.isURIResource()) {
                    row[i] = val.asResource().getURI();
                } else {
                    row[i] = "[]";
                }
            }
            data[count++] = row;
        }

        return new ArrayDatatable(data);
    }

    private Map<String, String> flatten(MultivaluedMap<String, String> parameters) {
        Map<String, String> flatParams = new HashMap<String, String>();
        for (String key : parameters.keySet()) {
            flatParams.put(key, parameters.getFirst(key));
        }
        return flatParams;
    }
}
