/******************************************************************
 * File:        TestMemStore.java
 * Created by:  Dave Reynolds
 * Created on:  4 Mar 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.stores;

import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class TestMemStore {

    @Test
    public void testBasicMemstore() {
        MemStore store = new MemStore();
        store.init(new HashMap<String, String>(), null);
        
//        Model m = store.getSafeNamedModel("http://example.com/test");
//        Model m = store.getUnionModel();
        Model m = store.asDataset().getDefaultModel();
        FileManager.get().readModel(m, "src/test/data/blue.ttl");
        assertEquals(3, m.size());
        assertEquals(3, store.getUnionModel().size());
    }
}
