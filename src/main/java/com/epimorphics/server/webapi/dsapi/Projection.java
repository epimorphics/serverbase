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
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.*;

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
    
    public Projection slice(int offset, int limit) {
        Projection slice = new Projection(api, data, sortSignature);
        slice.setOffset(offset);
        slice.setLimit(limit);
        return slice;
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
            for (DSAPIComponent c : api.components) {
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
}
