/******************************************************************
 * File:        SPARQLFilterQuery.java
 * Created by:  Dave Reynolds
 * Created on:  8 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.server.general.PrefixService;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Assembles the query needed to project a set of data cube observations.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO reimplement  

public class SPARQLFilterQuery {
    public static final String OBS_VAR = "?obs";
    
    protected StringBuffer query = new StringBuffer();
    
    public SPARQLFilterQuery() {
        query.append("SELECT * WHERE {\n");
    }
    
    public void addFilter(DSAPIComponent component, Range range) {
        query.append("    " + range.filterQuery(component) + "\n");
    }
    
    public void addQuery(DSAPIComponent component) {
        query.append(String.format("    %s %s ?%s.\n", OBS_VAR, component.getId(), component.getVarname()));
    }
    
    public String getQuery() {
        PrefixMapping pm = PrefixService.get().getPrefixes();
        return PrefixUtils.expandQuery( query.toString() + "}", pm);
    }
}
