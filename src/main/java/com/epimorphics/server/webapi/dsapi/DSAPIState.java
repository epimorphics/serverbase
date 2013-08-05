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

import javax.ws.rs.core.MultivaluedMap;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

/**
 * Represents a Dataset display state from a request. May be used to
 * define a projection or to refine the context for component range computation.
 * Can be set from query/post parameters or a json payload. Only single valued
 * query parameters are allowed for compatibility with JSON format.
 *  
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DSAPIState {
    public static final String TEXT_SEARCH_PARAM = "_text";
    public static final String SORT_PARAM        = "_sort";
    public static final String OFFSET_PARAM      = "_offset";
    public static final String LENGTH_PARAM      = "_length";
    public static final String LANG_FILTER_PARAM = "_lang";
    public static final String FACET_COUNTS_PARAM = "_withCounts";
    
    protected Map<String, String> state = new HashMap<String, String>();
    
    public DSAPIState() {
    }
    
    public DSAPIState(MultivaluedMap<String, String> params) {
        for (String key : params.keySet()) {
            // TODO parse strings to structure
            state.put(key, params.getFirst(key));
        }
    }
    
    
    public DSAPIState(JsonObject jstate) {
        for (Entry<String, JsonValue> ent : jstate.entrySet()) {
            // TODO parse json structure to internal structure
            state.put(ent.getKey(), ent.getValue().getAsString().toString());
        }
    }
    
    // TODO represent range filter structure
    
    
    
}
