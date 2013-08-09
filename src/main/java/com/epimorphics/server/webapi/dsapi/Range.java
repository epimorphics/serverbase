/******************************************************************
 * File:        DSAPIRange.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import static com.epimorphics.server.webapi.dsapi.JSONConstants.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.epimorphics.util.EpiException;


/**
 * Base class representing the range of a dataset api component.
 * Subclasses include support for both describing a range and for
 * requesting a narrower range.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class Range implements JSONWritable {
    // Done as a base class instead of an interface to force all child classes to be JSONWritable 

    public static Range create(JsonObject spec) {
        ResourceCache rc = ResourceCache.get();
        if (spec.hasKey(ONEOF)) {
            List<Value> args = new ArrayList<>();
            for(Iterator<JsonValue> i = spec.get(ONEOF).getAsArray().iterator(); i.hasNext();) {
                args.add( valueFromIDObject(i.next(), rc) );
            }
            return new RangeOneof(args);
        } else if (spec.hasKey(ID)) {
            // A single resource value
            return new RangeOneof( valueFromIDObject(spec, rc) );
        } else if (spec.hasKey(LT) ||spec.hasKey(LE) ||spec.hasKey(GT) ||spec.hasKey(GE)) {
            RangeBounds range = new RangeBounds();
            if (spec.hasKey(LT)) range.setLt( new NumberValue( spec.get(LT)) );
            if (spec.hasKey(LE)) range.setLe( new NumberValue( spec.get(LE)) );
            if (spec.hasKey(GT)) range.setGt( new NumberValue( spec.get(GT)) );
            if (spec.hasKey(GE)) range.setGe( new NumberValue( spec.get(GE)) );
            return range;
        }
        // TODO other range cases
        return null;
    }
    
    private static ResourceValue valueFromIDObject(JsonValue v, ResourceCache rc) {
        if (v instanceof JsonObject) {
            String id = ((JsonObject)v).get(ID).getAsString().value();
            return rc.valueFromID(id); 
        } else {
            throw new EpiException("Expected JsonObject at: " + v);
        }
    }
     
    public String filterQuery(DSAPIComponent c) {
        // Default is no filter
        return SPARQLFilterQuery.OBS_VAR + " " + c.getId() + " ?" + c.getVarname() + " .";
    }

    @Override
    public abstract void writeTo(JSFullWriter out);
    

}
