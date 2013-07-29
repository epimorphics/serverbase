/******************************************************************
 * File:        DSDTest.java
 * Created by:  Dave Reynolds
 * Created on:  29 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.hp.hpl.jena.rdf.model.Model;


/**
 * Simplified example of rendering a resource in a JSON format.
 * Required parameters: storename (config?) and uri (query).
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
@Path("/system/describe")
public class Describe {

    @GET
//    @Produces({FULL_MIME_TURTLE, FULL_MIME_RDFXML, JSONLDSupport.FULL_MIME_JSONLD})
    public Model describe(@QueryParam("store") String store, @QueryParam("uri") String uri) {
        return null;
    }
}
