/******************************************************************
 * File:        BaseEndpoint.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2012
 * 
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi;

import static com.epimorphics.webapi.marshalling.RDFXMLMarshaller.MIME_RDFXML;
import static com.epimorphics.webapi.marshalling.TurtleMarshaller.MIME_TURTLE;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;

/**
 * Useful base class from which Jersey endpoints can inherit.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class BaseEndpoint {

    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;

    /**
     * Load an RDF payload from a POST/PUT request. Returns null if it doesn't understand
     * the MIME type so that subclasses can extend it.
     */
    public Model getBodyModel(HttpHeaders hh, InputStream body) {
        String mime = hh.getMediaType().toString();
        String lang = null;
        if ( MIME_RDFXML.equals( mime ) ) {
            lang = FileUtils.langXML;
        } else if ( MIME_TURTLE.equals( mime ) ) {
            lang = FileUtils.langTurtle;
        } else {
            return null;
        }
        Model m = ModelFactory.createDefaultModel();
        m.read(body, null, lang);
        return m;
    }

}
