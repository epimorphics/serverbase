/******************************************************************
 * File:        RangeHierarchyTop.java
 * Created by:  Dave Reynolds
 * Created on:  9 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import static com.epimorphics.server.webapi.dsapi.JSONConstants.*;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.server.general.PrefixService;
import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Range used to report hierarchical values. Points to an API
 * which through which the scheme or collection can be browsed.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO this example shows that range reporting and range filtering out to be separated

public class RangeHierarchyTop extends Range {
    protected String id;
    protected String label;
    protected String description;
    protected String api;

    public RangeHierarchyTop(Resource codelist, DSAPI dsapi) {
        this.id = PrefixService.get().getResourceID(codelist);
        this.label = RDFUtil.getLabel(codelist);
        this.description = RDFUtil.getDescription(codelist);
        api = dsapi.getMan().getApiBase() + "/codelist/" + id;
    }
    
    
    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.pair(ID, id);
        out.pair(LABEL, label);
        out.pair(DESCRIPTION, description);
        out.pair(API, api);
        out.finishObject();
    }

}
