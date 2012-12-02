/******************************************************************
 * File:        Service.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.core;

import java.util.Map;

import javax.servlet.ServletContext;

/**
 * Generic notion of a service that can be instantiated as part of a web configuration.
 * Implementations should have a null constructor and perform all initialisation
 * via init() which will be passed configuration information.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Service {

    void init(Map<String, String> config, ServletContext context);

    void postInit();
}
