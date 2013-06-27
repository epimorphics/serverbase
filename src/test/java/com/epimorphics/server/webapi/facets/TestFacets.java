/******************************************************************
 * File:        TestFacets.java
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

import static org.junit.Assert.*;

public class TestFacets {

    @Test
    public void testBasicFacets() {
        FacetService service = new FacetService();
        Map<String, String> config = new HashMap<>();
        config.put("specFile", "dataset-facets.ttl");
        config.put("store", "store");
        service.init(config, null);

        List<FacetSpec> facetSpecs = service.specList;
        assertEquals(2, facetSpecs.size());
        FacetSpec spec = facetSpecs.get(0);
        assertEquals("Type", spec.getName());
        assertEquals("type", spec.getVarname());
        assertEquals("<http://purl.org/linked-data/registry#itemClass>", spec.getPropertyPath());

        Model testData = FileManager.get().loadModel("facet-test.ttl");
//        check(service, testData, null, 4);
        check(service, testData, "type=<http://example.com/test#type1>", 2);
        check(service, testData, "type=<http://example.com/test#type2>", 2);
        check(service, testData, "type=<http://example.com/test#type3>", 1);
        check(service, testData, "category=<http://example.com/test#cat2>", 2);
        check(service, testData, "type=<http://example.com/test#type1>|category=<http://example.com/test#cat2>", 1);
    }

    private void check(FacetService service, Model testData, String state, int expected) {
        FacetResult result = new FacetResult(service.baseQuery, state, service.specList, testData);
        List<FacetResultEntry> page = result.getResultsPage(0);
        assertEquals(expected, page.size());
    }
}
