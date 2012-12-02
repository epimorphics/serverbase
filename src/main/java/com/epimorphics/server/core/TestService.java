/******************************************************************
 * File:        Temp.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.core;

import java.util.Map;

import javax.servlet.ServletContext;

public class TestService implements Service {

    @Override
    public void init(Map<String, String> config, ServletContext context) {
        System.out.println("Test service init called");
        for (String conf : config.keySet()) {
            System.out.println(" " + conf + " = " + config.get(conf));
        }
    }

    @Override
    public void postInit() {
        System.out.println("Post init called");
    }

}
