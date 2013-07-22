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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.jena.fuseki.server.DatasetRef;
import org.apache.jena.fuseki.server.DatasetRegistry;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Store implementation in memory, used for test harnesses.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class MemStore extends StoreBase {
    static final Logger log = LoggerFactory.getLogger( MemStore.class );

    public static final String QUERY_ENDPOINT_PARAM    = "ep";

    protected Dataset dataset;
    protected Model unionModel;
    
    @Override
    public void init(Map<String, String> config, ServletContext context) {
        super.init(config, context);
        dataset = DatasetFactory.createMem();

        String qEndpoint = config.get(QUERY_ENDPOINT_PARAM);
        if (qEndpoint != null) {
            String base = context.getContextPath();
            if ( ! base.endsWith("/")) {
                base += "/";
            }
            base += qEndpoint;
            DatasetRef ds = new DatasetRef();
            ds.name = qEndpoint;
            ds.queryEP.add( base + "/query" );
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
            Model m = getSafeNamedModel(graphname);
            m.add(graph);
            m.setNsPrefixes(graph);
        } finally {
            unlock();
        }
    }

    protected Model getSafeNamedModel(String graphname) {
        Model m = dataset.getNamedModel(graphname);
        if (m == null) {
            m = ModelFactory.createDefaultModel();
            dataset.addNamedModel(graphname, m);
        }
        return m;
    }


    @Override
    protected
    void doDeleteGraph(String graphname) {
        lockWrite();
        try {
            dataset.removeNamedModel(graphname);
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
            getSafeNamedModel(graphname).read(input, graphname, lang.getName());
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
        if (unionModel == null) {
            Map<String, String> prefixes = new HashMap<String, String>();
            int count = 0;
            for (Iterator<String> i = dataset.listNames(); i.hasNext();) {
                i.next();
                count++;
            }
            Graph[] graphs = new Graph[ count ];
    
            count = 0;
            for (Iterator<String> i = dataset.listNames(); i.hasNext();) {
                Model m = dataset.getNamedModel( i.next() );
                graphs[count++] = m.getGraph();
                prefixes.putAll( m.getNsPrefixMap() );
            }
    
            unionModel = ModelFactory.createModelForGraph( new MultiUnion(graphs) );
            unionModel.setNsPrefixes(prefixes);
        } 
        return unionModel;
    }

}
