/******************************************************************
 * File:        DSAPIRange.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;


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
        // TODO
        return null;
    }
    
    public abstract String filterQuery(DSAPIComponent c);

    @Override
    public abstract void writeTo(JSFullWriter out);
    

}
