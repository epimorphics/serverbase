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

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.server.core.Service;
import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.server.core.Store;
import com.epimorphics.server.indexers.LuceneIndex;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;

/**
 * Useful base class from which Jersey RDF endpoints can inherit.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class BaseEndpoint {
    
    public static final String MIME_TURTLE = "text/turtle";

    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;

    /**
     * Load an RDF payload from a POST/PUT request. Returns null if it doesn't understand
     * the MIME type so that subclasses can extend it.
     */
    public Model getBodyModel(HttpHeaders hh, InputStream body) {
        if (hh.getMediaType() == null) return null;
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


    /**
     * Load an RDF payload from a POST/PUT request.
     * Throw an error if no acceptable RDF payload
     */
    public Model getSafeBodyModel(HttpHeaders hh, InputStream body) {
        Model m = getBodyModel(hh, body);
        if (m == null) {
            throw new WebApiException(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Non-RDF mime type found in request");
        } else {
            return m;
        }
    }

    /**
     * Return the default store, error if there is no such store configured
     */
    public Store getDefaultStore() {
        Store store = ServiceConfig.get().getDefaultStore();
        if (store == null) {
            throw new WebApiException(Response.Status.INTERNAL_SERVER_ERROR, "No default store configured");
        } else {
            return store;
        }
    }

    /**
     * Utility which treats the request body as an RDF model
     * and adds it to the a named graph within the default store.
     */
    public void putToDefaultStore(HttpHeaders hh, InputStream body, String graphname) {
        getDefaultStore().updateGraph(graphname, getSafeBodyModel(hh, body));
    }

    /**
     * Find the given lucene index
     */
    public LuceneIndex getIndex(String name) {
        Service s = ServiceConfig.get().getService(name);
        if (s != null && s instanceof LuceneIndex) {
            return (LuceneIndex)s;
        } else {
            throw new EpiException("Can't find indexer");
        }
    }
    
}
