/******************************************************************
 * File:        DSAPIRangeOneof.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;

/**
 * Represents a range which is a list of values, either simple literal values
 * or resource values.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RangeOneof extends Range {
    protected List<Value> values;
    
    public RangeOneof() {
        values = new ArrayList<>();
    }
    
    public RangeOneof(List<Value> values) {
        this.values = values;
    }
    
    public void addValue(Value value) {
        this.values.add(value);
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.key("oneof");
        out.startArray();
        for (Value v : values) {
            v.writeTo(out);
            out.arraySep();
        }
        out.finishArray();
        out.finishObject();
    }
    
}
