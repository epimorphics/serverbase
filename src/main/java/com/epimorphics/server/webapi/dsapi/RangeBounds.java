/******************************************************************
 * File:        RangeBounds.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;

/**
 * A range comprising one or more of an upper and lower bound on numeric values.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RangeBounds extends Range {
    protected NumberValue lt;
    protected NumberValue le;
    protected NumberValue gt;
    protected NumberValue ge;
    
    public RangeBounds() {
    }
    
    public RangeBounds(NumberValue lower, NumberValue upper) {
        ge = lower;
        le = upper;
    }

    public NumberValue getLt() {
        return lt;
    }

    public void setLt(NumberValue lt) {
        this.lt = lt;
    }

    public NumberValue getLe() {
        return le;
    }

    public void setLe(NumberValue le) {
        this.le = le;
    }

    public NumberValue getGt() {
        return gt;
    }

    public void setGt(NumberValue gt) {
        this.gt = gt;
    }

    public NumberValue getGe() {
        return ge;
    }

    public void setGe(NumberValue ge) {
        this.ge = ge;
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        emit("le", le, out);
        emit("lt", lt, out);
        emit("ge", ge, out);
        emit("gt", gt, out);
        out.finishObject();
    }
    
    private void emit(String key, NumberValue v, JSFullWriter out) {
        if (v != null) {
            out.key(key);
            v.writeTo(out);
        }
    }

    @Override
    public String filterQuery(DSAPIComponent c) {
        StringBuffer q = new StringBuffer();
        String vn = c.getVarname();
        q.append( SPARQLFilterQuery.OBS_VAR + " " + c.getId() + " ?" + vn );
        q.append(" . ");
        q.append("FILTER(");
        boolean started = false;
        started = addFilter(q, vn, ">", gt, started);
        started = addFilter(q, vn, ">=", ge, started);
        started = addFilter(q, vn, "<", lt, started);
        started = addFilter(q, vn, "<=", le, started);
        q.append(")");
        return q.toString();
    }

    private boolean addFilter(StringBuffer q, String varname, String op, NumberValue bound, boolean started) {
        if (bound != null) {
            if (started) {
                q.append(" && ");
            }
            q.append("?");
            q.append(varname);
            q.append(" ");
            q.append(op);
            q.append(" ");
            q.append( bound.asSPARQL() );
            return true;
        } else {
            return started;
        }
    }
}
