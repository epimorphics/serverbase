/******************************************************************
 * File:        TDBStore.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.stores;

import java.util.Map;

import org.apache.jena.fuseki.server.DatasetRef;
import org.apache.jena.fuseki.server.DatasetRegistry;

import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.FileUtil;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Store implementation using TDB.
 *
 * <p>Set "location" parameter to define where the
 * TDB store sites, use ${webapp} in the parameter value to reference the directory
 * where the webapp is installed.</p>
 *
 * <p>Set "union=true" to set the (currently GLOBAL) flag to make
 * sparql queries see the default graph as the union of all the named graphs.</p>
 *
 * <p>Set "ep={ds}" to register a fuseki query endpoint /ds/query.</p>
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TDBStore extends StoreBase {
    public static final String LOCATION_PARAM = "location";
    public static final String UNION_PARAM    = "union";
    public static final String QUERY_ENDPOINT_PARAM    = "ep";

    protected Dataset dataset;

    @Override
    public void init(Map<String, String> config) {
        String location = config.get(LOCATION_PARAM);
        if (location == null) {
            throw new EpiException("No location defined for TDB");
        }
        location = ServiceConfig.get().expandFileLocation(location);
        FileUtil.ensureDir(location);
        dataset = TDBFactory.createDataset( location );

        if ("true".equalsIgnoreCase( config.get(UNION_PARAM) )) {
            TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;
        }

        String qEndpoint = config.get(QUERY_ENDPOINT_PARAM);
        if (qEndpoint != null) {
            String base = "/" + qEndpoint;
            DatasetRef ds = new DatasetRef();
            ds.name = qEndpoint;
            ds.queryEP.add( base + "/query" );
            ds.dataset = dataset.asDatasetGraph();

            DatasetRegistry.get().put(base, ds);
        }
    }

    @Override
    protected
    void doAddGraph(String graphname, Model graph) {
        lockWrite();
        try {
            dataset.getNamedModel(graphname).add(graph);
        } finally {
            unlock();
        }
    }

    @Override
    protected
    void doUpdateGraph(String graphname, Model graph) {
        lockWrite();
        try {
            Model store = dataset.getNamedModel(graphname);
            store.removeAll();
            store.add( graph );
        } finally {
            unlock();
        }
    }

    @Override
    protected
    void doDeleteGraph(String graphname) {
        lockWrite();
        try {
            Model store = dataset.getNamedModel(graphname);
            store.removeAll();
        } finally {
            unlock();
        }
    }

    @Override
    public Dataset asDataset() {
        return dataset;
    }

}
