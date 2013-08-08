/******************************************************************
 * File:        DSAPIManager.java
 * Created by:  Dave Reynolds
 * Created on:  1 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.server.core.Service;
import com.epimorphics.server.core.ServiceBase;
import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.server.core.Store;
import com.epimorphics.server.general.PrefixService;
import com.epimorphics.server.webapi.dsapi.DSAPI;
import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.Cube;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Makes a collection data-cube-like datasets available over a flexible
 * query API. May be generalized to remove the QB-specific notions.
 * Current version is configured by pointing to a store and it will
 * generate api instances for each dataset.
 *  
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DSAPIManager extends ServiceBase implements Service, JSONWritable {
    static Logger log = LoggerFactory.getLogger(DSAPIManager.class);
    
    protected static final String STORE_PARAM = "store";
    protected static final String API_BASE = "apiBase";

    protected Store store;
    protected Map<String, DSAPI> datasets;
    protected String apiBase;
    
    @Override
    public void postInit() {
        String storeName = getRequiredParam(STORE_PARAM);
        store = ServiceConfig.get().getServiceAs(storeName, Store.class);
        if (store == null) {
            throw new EpiException("Can't find configured store: " + storeName);
        }
        apiBase = getRequiredFileParam(API_BASE);
    }
    
    private Map<String, DSAPI> getDatasets() {
        if (datasets == null) {
            datasets = new HashMap<String, DSAPI>();
            extractDatasets(store);
        }
        return datasets;
    }
    
    public Set<String> listDatasets() {
        return getDatasets().keySet();
    }
    
    public DSAPI getDataset(String id) {
        return getDatasets().get(id);
    }

    public String getApiBase() {
        return apiBase;
    }

    public Store getStore() {
        return store;
    }

    private void extractDatasets(Store store) {
        store.lock();
        try {
            Model model = store.getUnionModel();
            StmtIterator i = model.listStatements(null, Cube.structure, (RDFNode)null);
            while (i.hasNext()) {
                Statement s = i.next();
                Resource dataset = s.getSubject();
                RDFNode dsdN = s.getObject();
                if (!dsdN.isResource()) {
                    throw new EpiException("Dataset declares its structure as a literal: " + dataset);
                }
                Resource dsd = dsdN.asResource();
                String id = PrefixService.get().getResourceID(dataset);
                datasets.put(id, new DSAPI(this, dataset, dsd));
                log.info("Registering Dataset API for: " + id);
            }
        } finally {
            store.unlock();
        }
    }
    
    @Override
    public void writeTo(JSFullWriter out) {
        getDatasets();
        out.startArray();
        for (String key : datasets.keySet()) {
            datasets.get(key).writeTo(out);
        }
        out.finishArray();
    }
    
}
