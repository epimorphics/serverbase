/******************************************************************
 * File:        Projection.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;

/**
 * A table of API results. Can be sorted, sliced, cached.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Projection {
    protected DSAPI api;
    protected Object[][] data;
    protected String sortSignature;
    
    public Projection(DSAPI api) {
        this.api = api;
    }
    
    public Projection(DSAPI api, Object[][] data) {
        this.api = api;
        this.data = data;
    }
    
    public Projection(DSAPI api, Object[][] data, String initialSort) {
        this.api = api;
        this.data = data;
        this.sortSignature = initialSort;
    }
    
    public Projection sort(String sort) {
        if (sort.equals(sortSignature)) {
            return this;
        } else {
            // TODO create new sorted projection
            return null;
        }
    }
    
    // TODO load up data from a SPARQL result set with query mapping
    
    public void writeSlice(JSFullWriter out, int offset, int length) {
        // TODO write out slice
    }
    
    public void writeAll(JSFullWriter out) {
        writeSlice(out, 0, data.length);
    }
}
