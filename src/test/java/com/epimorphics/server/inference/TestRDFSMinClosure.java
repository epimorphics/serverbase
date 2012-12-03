/******************************************************************
 * File:        TestRDFSMinClosure.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2012
 * 
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.inference;

import org.junit.Test;
import static org.junit.Assert.*;

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
