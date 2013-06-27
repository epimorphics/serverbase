/******************************************************************
 * File:        TestRDFSMinClosure.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2012
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

package com.epimorphics.server.inference;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class TestRDFSMinClosure {

    @Test
    public void testBasics() {
        Model ontology = FileManager.get().loadModel("src/test/resources/ontology.ttl");
        Model data =  FileManager.get().loadModel("src/test/resources/testAbox.ttl");
        RDFSMinClosure closureMutator = new RDFSMinClosure(ontology);
        closureMutator.mutate(data);
        assertTrue( data.isIsomorphicWith( FileManager.get().loadModel("src/test/resources/testAboxClosure.ttl")) );
    }
}
