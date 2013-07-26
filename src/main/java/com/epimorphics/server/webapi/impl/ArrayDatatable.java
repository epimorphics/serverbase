/******************************************************************
 * File:        ArrayDatatable.java
 * Created by:  Dave Reynolds
 * Created on:  25 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.impl;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents a sortable, filterable datatable as a flat array of rows
 * of simple (comparable) values.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ArrayDatatable {

    protected Object[][] data;
    protected String sortSignature;
    
    public ArrayDatatable(Object[][] data) {
        this.data = data;
        sortSignature = "";
    }
    
    // Only call from thread safe method
    private void sort(DatatableProjection projection) {
        if (!sortSignature.equals(projection.getSortSignature())) {
            Arrays.sort(data, projection);
            sortSignature = projection.sortSignature;
        }
    }
    
    // Only call from thread safe method
    private Object[][] slice(Object[][] fdata, DatatableProjection projection) {
        int length = Math.min(projection.getLength(), fdata.length - projection.getOffset());
        Object[][] result = new Object[length][];
        for (int i = 0; i < length; i++) {
            result[i] = fdata[i + projection.getOffset()];
        }
        return result;
    }

    private Object[][] filtered(DatatableProjection projection) {
        if (projection.hasFilter()) {
            ArrayList<Object[]> filtered = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                if (projection.accept(data[i])) {
                    filtered.add( data[i] );
                }
            }
            return filtered.toArray(new Object[filtered.size()][]);
        } else {
            return data;
        }
    }
        
    public synchronized DatatableResponse project(DatatableProjection projection) {
        sort(projection);
        Object[][] filtered = filtered(projection);
        Object[][] result = slice(filtered, projection);
        DatatableResponse response = new DatatableResponse();
        response.setiTotalDisplayRecords( filtered.length );
        response.setiTotalRecords( data.length );
        response.setsEcho( projection.getsEcho() );
        response.setAaData( result );
        return response;
    }
    
}
