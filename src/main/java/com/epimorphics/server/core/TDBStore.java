/******************************************************************
 * File:        TDBStore.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.core;

import java.util.Map;

import com.epimorphics.util.EpiException;
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
 * <p>Set "union-default=true" to set the (currently GLOBAL) flag to make
 * sparql queries see the defaul graph as the union of all the named graphs.</p>
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TDBStore extends StoreBase {
    public static final String LOCATION_PARAM = "location";
    public static final String UNION_PARAM    = "union-default";

    protected Dataset dataset;

    @Override
    public void init(Map<String, String> config) {
        String location = config.get(LOCATION_PARAM);
        if (location == null) {
            throw new EpiException("No location defined for TDB");
        }
        location = ServiceConfig.expandFileLocation(location);
        dataset = TDBFactory.createDataset( location );

        if ("true".equalsIgnoreCase( config.get(UNION_PARAM) )) {
            TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;
        }
    }

    @Override
    void doAddGraph(String graphname, Model graph) {
        lockWrite();
        try {
            dataset.getNamedModel(graphname).add(graph);
        } finally {
            unlock();
        }
    }

    @Override
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
