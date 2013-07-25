/******************************************************************
 * File:        ArrayDatatable.java
 * Created by:  Dave Reynolds
 * Created on:  25 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.impl;

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
    
    // Only call from thread safe wrappers
    private void sort(DatatableProjection projection) {
        if (!sortSignature.equals(projection.getSortSignature())) {
            Arrays.sort(data, projection);
            sortSignature = projection.sortSignature;
        }
    }
    
    protected Object[][] getProjection(DatatableProjection projection) {
        sort(projection);
        if (projection.hasFilter()) {
            Object[][] result = new Object[projection.getLength()][];
            int count = 0;
            for (int i = 0; i < data.length && count < projection.getLength(); i++) {
                if (projection.accept(data[i])) {
                    result[count] = data[i];
                    count++;
                }
            }
            if (count < result.length) {
                Object[][] resultTrunc = new Object[count][];
                for (int i = 0; i < count; i++) {
                    resultTrunc[i] = result[i];
                }
                result = resultTrunc;
            }
            return result;
        } else {
            int length = Math.min(projection.getLength(), data.length);
            Object[][] result = new Object[length][];
            for (int i = 0; i < length; i++) {
                result[i] = data[i];
            }
            return result;
        }
    }
    
    public synchronized DatatableResponse project(DatatableProjection projection) {
        Object[][] result = getProjection(projection);
        DatatableResponse response = new DatatableResponse();
        response.setiTotalDisplayRecords( result.length );
        response.setiTotalRecords( data.length );
        response.setsEcho( projection.getsEcho() );
        response.setAaData( result );
        return response;
    }
}
