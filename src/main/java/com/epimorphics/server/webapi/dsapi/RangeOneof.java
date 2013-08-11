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
import java.util.Iterator;
import java.util.List;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.*;

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
    
    public RangeOneof(Value value) {
        this.values.add(value);
    }
    
    public void addValue(Value value) {
        this.values.add(value);
    }
    
    /**
     * If the range is a singleton then return that single value, otherwise return null;
     */
    public Value getSingletonValue() {
        if (values.size() == 1) {
            return values.get(0);
        }
        return null;
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.key(ONEOF);
        out.startArray();
        for (Iterator<Value> i = values.iterator(); i.hasNext();) {
            i.next().writeTo(out);
            if (i.hasNext()) {
                out.arraySep();
            }
        }
        out.finishArray();
        out.finishObject();
    }

    @Override
    public String filterQuery(DSAPIComponent c) {
        if (values.size() == 1) {
            return SPARQLFilterQuery.OBS_VAR + " " + c.asSPARQL() + " " + values.get(0).asSPARQL() + " .";
        } if (values.isEmpty()) {
            return SPARQLFilterQuery.OBS_VAR + " " + c.asSPARQL() + " ?" + c.getVarname() + " .";
        } else {
            StringBuffer q = new StringBuffer();
            q.append( SPARQLFilterQuery.OBS_VAR + " " + c.asSPARQL() + " ?" + c.getVarname() );
            q.append(" . ");
            q.append("FILTER( ?" + c.getVarname() + " IN (");
            for (Iterator<Value> i = values.iterator(); i.hasNext();) {
                q.append( i.next().asSPARQL() );
                if (i.hasNext()) q.append(", ");
            }
            q.append(") )");
            return q.toString();
        }
    }
    
}
