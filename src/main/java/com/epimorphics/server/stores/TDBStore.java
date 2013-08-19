/******************************************************************
 * File:        TDBStore.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *****************************************************************/

package com.epimorphics.server.stores;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.jena.fuseki.server.DatasetRef;
import org.apache.jena.fuseki.server.DatasetRegistry;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    static final Logger log = LoggerFactory.getLogger( TDBStore.class );

    public static final String LOCATION_PARAM = "location";
    public static final String UNION_PARAM    = "union";
    public static final String QUERY_ENDPOINT_PARAM    = "ep";

    protected Dataset dataset;

    @Override
    public void init(Map<String, String> config, ServletContext context) {
        super.init(config, context);
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
            String base = context.getContextPath();
            if ( ! base.endsWith("/")) {
                base += "/";
            }
            base += qEndpoint;
            DatasetRef ds = new DatasetRef();
            ds.name = qEndpoint;
//            ds.queryEP.add( base + "/query" );
            ds.dataset = dataset.asDatasetGraph();
            DatasetRegistry.get().put(base, ds);
            log.info("Installing SPARQL query endpoint at " + base + "/query");
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

    @Override
    protected void doAddGraph(String graphname, InputStream input,
            String mimeType) {
        Lang lang = WebContent.contentTypeToLang(mimeType);
        if (lang == null) {
            throw new EpiException("Cannot read MIME type: " + mimeType);
        }
        lockWrite();
        try {
            dataset.getNamedModel(graphname).read(input, graphname, lang.getName());
            try { input.close(); } catch (IOException eio) {}
        } catch (Exception e) {
            abort();
            throw new EpiException(e);
        } finally {
            unlock();
        }
    }

    @Override
    public Model getUnionModel() {
        return dataset.getNamedModel("urn:x-arq:UnionGraph");
    }

}
