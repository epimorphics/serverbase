/******************************************************************
 * File:        ServiceBase.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.core;

import java.util.Map;

import javax.servlet.ServletContext;

import com.epimorphics.util.EpiException;

public class ServiceBase implements Service {

    protected Map<String, String> config;

    @Override
    public void init(Map<String, String> config, ServletContext context) {
        this.config = config;
    }

    @Override
    public void postInit() {
    }

    protected String getRequiredParam(String param) {
        String location = config.get(param);
        if (location == null) {
            throw new EpiException("Missing requried configuration parameter: " + param);
        }
        return location;
    }

    protected String getRequiredFileParam(String param) {
        return ServiceConfig.get().expandFileLocation( getRequiredParam(param)) ;
    }

}