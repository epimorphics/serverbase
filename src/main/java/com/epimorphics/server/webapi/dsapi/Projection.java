/******************************************************************
 * File:        Projection.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import static com.epimorphics.server.webapi.dsapi.JSONConstants.DATA;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.SIZE;

import java.util.Arrays;
import java.util.Comparator;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.epimorphics.util.EpiException;

/**
 * A table of API results. Can be sorted, sliced, cached.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO handle aggregation of multi-valued attributes

public class Projection implements JSONWritable {
    protected DSAPI api;
    protected Object[][] data;
    protected String sortSignature;
    protected int offset = 0;
    protected int length = Integer.MAX_VALUE; 
    
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
    
    public Projection(Projection clone, Object[][] data, String initialSort) {
        this.api = clone.api;
        this.offset = clone.offset;
        this.length = clone.length;
        
        this.data = data;
        this.sortSignature = initialSort;
    }
    
    public Projection(Projection clone, int offset, int limit) {
        this.api = clone.api;
        this.data = clone.data;
        this.sortSignature = clone.sortSignature;
        
        this.offset = offset;
        this.length = limit;
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
            CustomSort comparator = new CustomSort(sort);
            if (comparator.isNull()) {
                return this;
            }
            Object[][] clone = new Object[data.length][];
            for (int i = 0; i < data.length; i++) {
                clone[i] = data[i];
            }
            Arrays.sort(clone, comparator);
            return new Projection(this, clone, sortSignature);
        }
    }
    
    public Projection slice(int offset, int limit) {
        return new Projection(this, offset, limit);
    }
    
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return length;
    }

    public void setLimit(int limit) {
        this.length = limit;
    }

    @Override
    public void writeTo(JSFullWriter out) {
        int limit = Math.min(data.length, length);
        
        out.startObject();
        out.pair(SIZE, data.length);
        out.pair(State.OFFSET_PARAM, offset);
        out.pair(State.LIMIT_PARAM, limit);
        
        out.key(DATA);
        out.startArray();
        for (int i = 0; i < limit; i++) {
            out.startObject();
            int j = 0;
            for (DSAPIComponent c : api.getComponents()) {
                out.key(c.getId());
                Value v = (Value)data[i][j++];
                v.writeTo(out);
            }
            out.finishObject();
            if (i < limit-1) {
                out.arraySep();
            }
        }
        out.finishArray();
        out.finishObject();
    }
    
    class CustomSort implements Comparator<Object[]> {
        protected int nSort;
        protected int[] sortColumns;
        protected boolean[] sortAscending;
        
        public CustomSort(String sortSig) {
            String[] sorts = sortSig.split(",");
            nSort = sorts.length;
            if (nSort == 0) {
                return;
            }
            sortColumns = new int[nSort];
            sortAscending = new boolean[nSort];
            for (int i = 0; i < nSort; i++) {
                String sort = sorts[i].trim();
                if (sort.endsWith("+")) {
                    sort = sort.substring(0, sort.length()-1);
                    sortAscending[i] = true;
                } else if (sort.endsWith("-")) {
                    sort = sort.substring(0, sort.length()-1);
                    sortAscending[i] = false;
                } else {
                    sortAscending[i] = true;
                }
                int ci = api.getComponentIndex(sort);
                if (ci == -1) {
                    throw new EpiException("Unrecognizable sort key: " + sort);
                } else {
                    sortColumns[i] = ci;
                }
            }
        }
        
        public boolean isNull() {
            return nSort == 0;
        }

        @Override
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
        
    }
}
