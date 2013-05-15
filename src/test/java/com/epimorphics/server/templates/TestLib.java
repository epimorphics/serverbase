/******************************************************************
 * File:        TestLib.java
 * Created by:  Dave Reynolds
 * Created on:  15 May 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.templates;

import java.util.Calendar;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import static org.junit.Assert.*;

public class TestLib {

    Lib lib = new Lib();
    
    @Test
    public void testIsDateTime() {
        Model m = ModelFactory.createDefaultModel();
        assertFalse(lib.isDatetime( m.createLiteral("fool")));
        assertFalse(lib.isDatetime( m.createTypedLiteral(42)));
        assertTrue(lib.isDatetime( m.createTypedLiteral( Calendar.getInstance() )));
    }
}
