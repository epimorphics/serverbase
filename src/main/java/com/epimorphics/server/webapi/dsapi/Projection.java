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

// TODO handle aggregation of multi-valued attributes

public class Projection {
    protected Object[][] data;
    protected String sortSignature;
    
    public Projection() {
    }
    
    public Projection(Object[][] data) {
        this.data = data;
    }
    
    public Projection(Object[][] data, String initialSort) {
        this.data = data;
        this.sortSignature = initialSort;
    }
   
    public void setSortSignature(String sort) {
        this.sortSignature = sort;
    }
    
    public String getSortSignature() {
        return sortSignature;
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
