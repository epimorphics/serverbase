/******************************************************************
 * File:        Shutdown.java
 * Created by:  Dave Reynolds
 * Created on:  9 Apr 2013
 *
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.core;

/**
 * Interface that signals a service which can be shutdown as
 * part of context exit.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Shutdown {

    public void shutdown();
}
