/******************************************************************
 * File:        WebApiException.java
 * Created by:  Dave Reynolds
 * Created on:  30 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Signal an application error response with a specifiable HTTP status code
 * and an associated message.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class WebApiException extends WebApplicationException {

    private static final long serialVersionUID = -5431338940458513195L;

    // TODO consider velocity rendering of HTML message

    public WebApiException(int status, String message) {
        super(Response.status(status).entity(message).type("text/plain").build());
    }

    public WebApiException(Response.Status status, String message) {
        this(status.getStatusCode(), message);
    }
}
