/******************************************************************
 * File:        BaseEndpoint.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *****************************************************************/

package com.epimorphics.server.webapi;

import static com.epimorphics.webapi.marshalling.RDFXMLMarshaller.MIME_RDFXML;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
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

    public static final String DUMMY_BASE_URI = "http://dummy.com";

//    public static final String SESSION_USER_KEY = "user";
//    public static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;
    protected @Context HttpServletRequest request;

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
        m.read(body, DUMMY_BASE_URI, lang);
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
//
//    /**
//     * FInd the name or IP address of the originator of this request
//     */
//    public String getRequestor() {
//        HttpSession session = request.getSession(false);
//        if (session != null) {
//            Object user = session.getAttribute(SESSION_USER_KEY);
//            if (user != null) {
//                return (String)user;
//            }
//        }
//
//        if (request.getHeader(FORWARDED_FOR_HEADER) != null) {
//            return request.getHeader(FORWARDED_FOR_HEADER);
//        }
//
//        return request.getRemoteAddr();
//    }

}
