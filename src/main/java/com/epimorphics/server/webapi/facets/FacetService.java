/******************************************************************
 * File:        FacetService.java
 * Created by:  Dave Reynolds
 * Created on:  20 May 2013
 *
 * (c) Copyright 2013, Epimorphics Limited
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

package com.epimorphics.server.webapi.facets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.server.core.Service;
import com.epimorphics.server.core.ServiceBase;
import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.server.core.Store;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.FacetVocab;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.enhanced.UnsupportedPolymorphismException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class FacetService extends ServiceBase implements Service {
    static Logger log = LoggerFactory.getLogger(FacetService.class);

    public static final String SPEC_FILE_PARAM = "specFile";
    public static final String STORE_PARAM = "store";

    protected String baseQuery;
    protected Store store;
    protected List<FacetSpec> specList = new ArrayList<>();

    @Override
    public void init(Map<String, String> config, ServletContext context) {
        super.init(config, context);

        String specFile = getRequiredFileParam(SPEC_FILE_PARAM);
        Model spec = FileManager.get().loadModel( specFile );
        parseFacetSpec(spec);
        log.info("Loaded facet specification from " + specFile);
    }

    @Override
    public void postInit() {
        String storename = getRequiredParam(STORE_PARAM);
        store = ServiceConfig.get().getServiceAs(storename, Store.class);
    }

    private void parseFacetSpec(Model spec) {
        ResIterator ri = spec.listSubjectsWithProperty(FacetVocab.facets);
        if (!ri.hasNext()) {
            throw new EpiException("Could not locate facet specification in file");
        }
        Resource root = ri.next();
        if (ri.hasNext()) {
            throw new EpiException("Ambiguous facet specification, found two roots");
        }
        try {
            Resource facetListR = root.getPropertyResourceValue(FacetVocab.facets);
            RDFList facets = facetListR.as(RDFList.class);
            for (RDFNode facet : facets.asJavaList()) {
                Resource facetR = facet.asResource();
                String label = RDFUtil.getLabel(facet);
                String varname = RDFUtil.getStringValue(facetR, SKOS.notation, label);
                String path = RDFUtil.getStringValue(facetR, FacetVocab.query);
                if (path == null) {
                    throw new EpiException("Could not find query for facet " + label);
                }
                specList.add( new FacetSpec(label, varname, path) );
            }
        } catch (UnsupportedPolymorphismException e) {
            throw new EpiException("Could not parse the list of facets as an RDF list");
        }
        baseQuery = RDFUtil.getStringValue(root, FacetVocab.query);
        if (baseQuery == null) {
            throw new EpiException("No base query specified");
        }
    }

    public FacetResult query(String state) {
        try {
            store.lock();
//            return new FacetResult(baseQuery, state, specList, store.getUnionModel());
            return new FacetResult(baseQuery, state, specList, store.asDataset().getDefaultModel());
        } finally {
            store.unlock();
        }
    }
}
