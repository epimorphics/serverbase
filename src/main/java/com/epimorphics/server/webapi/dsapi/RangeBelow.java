/******************************************************************
 * File:        RangeBelow.java
 * Created by:  Dave Reynolds
 * Created on:  9 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.*;

/**
 * Represents a hierarchical query which matches all entries below a given
 * parent node.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RangeBelow extends Range {
    protected ResourceValue parent;
    
    public RangeBelow(ResourceValue parent) {
        this.parent = parent;
    }
    
    public String filterQuery(DSAPIComponent c) {
        return String.format("%s %s ?%s. ?%s skos:broader + <%s>.",  SPARQLFilterQuery.OBS_VAR, c.getId(), c.getVarname(), c.getVarname(), parent.getUri());
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.key(BELOW);
        parent.writeTo(out);
        out.finishObject();
    }

}
