/******************************************************************
 * File:        RangeCollection.java
 * Created by:  Dave Reynolds
 * Created on:  15 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import static com.epimorphics.server.webapi.dsapi.JSONConstants.IN_COLLECTION;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;

public class RangeCollection extends Range {
    protected ResourceValue parent;
    
    public RangeCollection(ResourceValue parent) {
        this.parent = parent;
    }
    
    public String filterQuery(DSAPIComponent c) {
        return String.format("%s %s ?%s. <%s> skos:member ?%s .",  SPARQLFilterQuery.OBS_VAR, c.asSPARQL(), c.getVarname(), parent.getUri(), c.getVarname());
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.key(IN_COLLECTION);
        parent.writeTo(out);
        out.finishObject();
    }

}
