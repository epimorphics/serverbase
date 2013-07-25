/******************************************************************
 * File:        Datatable.java
 * Created by:  Dave Reynolds
 * Created on:  25 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.impl;

import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a sort/filter/slice specification for a server side datatable
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DatatableProjection implements Comparator<Object[]> {
    protected static final String PARAM_iDisplayStart = "iDisplayStart";
    protected static final String PARAM_iDisplayLength = "iDisplayLength";
    protected static final String PARAM_iColumns= "iColumns";
    protected static final String PARAM_sSearch= "sSearch";
    protected static final String PARAM_bRegex = "bRegex";
    protected static final String PARAM_iSortingCols = "iSortingCols";
    protected static final String PARAM_PREFIX_iSortCol = "iSortCol_";
    protected static final String PARAM_PREFIX_sSortDir = "sSortDir_";
    protected static final String PARAM_sEcho = "sEcho";

    protected int[] sortColumns;
    protected boolean[] sortAscending;
    protected int nSort;
    protected String filter;
    protected boolean filterIsRegex;
    protected Pattern filterRegex;
    protected int offset;
    protected int length;
    protected String sEcho;
    
    protected String sortSignature;

    public DatatableProjection() {
    }
    
    public DatatableProjection(Map<String, String> requestParams) {
        sEcho = requestParams.get(PARAM_sEcho);
        
        offset = Integer.parseInt( requestParams.get(PARAM_iDisplayStart) );
        length = Integer.parseInt( requestParams.get(PARAM_iDisplayLength) );
        
        filter = requestParams.get(PARAM_sSearch);
        if (filter != null) {
            filterIsRegex = Boolean.parseBoolean( requestParams.get(PARAM_bRegex) );
            filterRegex = Pattern.compile(filter);
        }
        
        nSort = Integer.parseInt( requestParams.get(PARAM_iSortingCols) );
        if (nSort > 0) {
            sortColumns = new int[nSort];
            sortAscending = new boolean[nSort];
            for (int i = 0; i < nSort; i++) {
                sortColumns[i] = Integer.parseInt( requestParams.get(PARAM_PREFIX_iSortCol + i) );
                sortAscending[i] = "asc".equalsIgnoreCase( requestParams.get(PARAM_PREFIX_sSortDir + i) );
            }
        }
        sortSignature = "";
        for (int i = 0; i < nSort; i++) {
            sortSignature += "" + sortColumns[i] + (sortAscending[i] ? "+" : "-"); 
        }
    }
    
    public boolean accept(Object[] row) {
        if (filter != null && !filter.isEmpty()) {
            String lcFilter = filter.toLowerCase();
            for (Object value : row) {
                // Expensive - may want to cache this if used a lot
                String valueS = value.toString().toLowerCase();
                if (filterIsRegex) {
                    if (filterRegex.matcher(valueS).matches()) {
                        return true;
                    }
                } else {
                    if (valueS.contains(filter)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }
    
    public int compare(Object[] row1, Object[] row2) {
        for (int i = 0; i < nSort; i++) {
            int col = sortColumns[i];
            @SuppressWarnings({ "unchecked", "rawtypes" })
            int comparison = ((Comparable)row1[col]).compareTo( (Comparable)row2[col] );
            if (comparison != 0) {
                return sortAscending[i] ? comparison : -comparison;
            }
        }
        return 0;
    }
    
    public String getSortSignature() {
        return sortSignature;
    }
    
    public boolean hasFilter() {
        return filter != null && !filter.isEmpty();
    }
    
    

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    
    public String getsEcho() {
        return sEcho;
    }

    @Override
    public String toString() {
        String lex = "DatatableProject: " + offset + "[" + length + "]";
        if (filter != null && !filter.isEmpty()) {
            lex += " - filter(" + filter + (filterIsRegex ? ", regex" : "") + ")";
        }
        if (sortColumns != null) { 
            lex += " - sort by" + sortSignature;
        }
        return lex;
    }
}
