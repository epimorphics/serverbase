/******************************************************************
 * File:        DSAPIState.java
 * Created by:  Dave Reynolds
 * Created on:  5 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.jena.atlas.json.JsonNumber;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.util.EpiException;

/**
 * Represents a Dataset display state from a request. May be used to
 * define a projection or to refine the context for component range computation.
 * Can be set from query/post parameters or a json payload. Only single valued
 * query parameters are allowed for compatibility with JSON format.
 *  
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class State {
    public static final String TEXT_SEARCH_PARAM = "_text";
    public static final String SORT_PARAM        = "_sort";
    public static final String OFFSET_PARAM      = "_offset";
    public static final String LIMIT_PARAM      = "_limit";
    public static final String LANG_FILTER_PARAM = "_lang";
    public static final String FACET_COUNTS_PARAM = "_withCounts";
    
    protected Map<String, Object> state = new HashMap<>();
    
    public State() {
    }
    
    public State(MultivaluedMap<String, String> params) {
        for (String key : params.keySet()) {
            // TODO parse strings to structure
            state.put(key, params.getFirst(key));
        }
    }
    
    public State(JsonObject jstate) {
        for (Entry<String, JsonValue> ent : jstate.entrySet()) {
            String key = ent.getKey();
            JsonValue value = ent.getValue();
            if (key.startsWith("_")) {
                state.put(key, value);
            } else {
                // A component filter
                Range range = null;
                if (value.isObject()) {
                    // Range or a single resource value
                    range = Range.create( value.getAsObject() );
                    if (range == null) {
                        throw new EpiException("Can't parse component filter: " + value);
                    }
                }
                state.put(key, range);
            }
        }
    }
    
    public boolean hasKey(String key) {
        return state.containsKey(key);
    }
    
    public Set<String> keySet() {
        return state.keySet();
    }
    
    /**
     * Return the filter range for this description, or null if there is no restriction
     */
    public Range getRangeFor(DSAPIComponent c) {
        Object range = state.get(c.getId());
        if (range instanceof Range) {
            return (Range)range;
        } else if (range == null) {
            return null;
        } else {
            throw new EpiException("Internal error: found non-Range value for component filter");
        }
    }
    
    /**
     * Return a state component as a string
     */
    public String getString(String key) {
        Object v = state.get(key);
        if (v != null) {
            if (v instanceof JsonString) {
                return ((JsonString)v).value();
            }
            return v.toString();
        } else {
            return null;
        }
    }
    
    /**
     * Return a state component as a integer, if possible, otherwise
     * return the default.
     */
    public int getInt(String key, int deflt) {
        Object v = state.get(key);
        if (v instanceof JsonNumber) {
            return ((JsonNumber)v).value().intValue();
        } else if (v instanceof Number) {
            return ((Number)v).intValue();
        } else if (v instanceof String) {
            return Integer.parseInt((String)v);
        } else {
            return deflt;
        }
    }
    
    
    // TODO create a signature for the filter/sort aspects of the state for cache indexing
    
}
