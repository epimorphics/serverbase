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

import com.hp.hpl.jena.rdf.model.Resource;

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
    public static final String LENGTH_PARAM      = "_length";
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
        ResourceCache rc = ResourceCache.get();
        for (Entry<String, JsonValue> ent : jstate.entrySet()) {
            String key = ent.getKey();
            JsonValue value = ent.getValue();
            if (key.startsWith("_")) {
                state.put(key, value);
            } else {
                // A component filter
                if (value.isObject()) {
                    // Range or a single resource value
                    JsonObject o = value.getAsObject();
                    if (o.hasKey("oneof")) {   // TODO factor out as constants
                        // TODO extract oneof list, create range and add it to the state map
                    // TODO other range cases
                    } else if (o.hasKey("@id")) {
                        // A single resource value
                        Resource r = rc.resourceFromID(o.get("@id").getAsString().value(), null);
                        // TODO actually need a resource value, have a way of generating cheap resource values for filter case
                        // TODO turn it into a oneof filter and add it to the state map
                    }
                }
            }
            // TODO parse json structure to internal structure
            state.put(ent.getKey(), ent.getValue());
        }
    }
    
    // TODO represent range filter structure
    
    // TODO create a signature for the filter/sort aspects of the state for cache indexing
    
}
