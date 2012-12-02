/******************************************************************
 * File:        VelocityRender.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2012
 * 
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.templates;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.velocity.app.VelocityEngine;

import com.epimorphics.server.core.Service;

/**
 * Service to provide velocity-based HTML rendering. Can be used
 * from Jersey restlets to format returned results. Can also be 
 * installed as a filter to render requests directly.
 * <p>
 * Configuration parameters:
 *  <ul>
 *    <li>templates - root directory where velocity templates are located, if it contains a file velocity.properties then
 *        that will be used to configure velocity, if it contains a file macros.vm then that will be used to define static global macros</li>
 *    <li>root - URL, relative to the webapp, where the velocity filter should be installed (so that a request {root}/foo will 
 *    test for a file foo.vm in the templates directory and render that, otherwise will forward the filter down the chain)</li>
 *  </ul>
 * </p>
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class VelocityRender implements Service {
    protected VelocityEngine ve;
    
    @Override
    public void init(Map<String, String> config, ServletContext context) {
        ve = new VelocityEngine();
        // TODO create and config velocity
        // TODO install filter
    }

    @Override
    public void postInit() {
    }

    // TODO rendering methods suitable for inline calls
    
    // TODO rendering methods to support the filter 
    
    /**
     * The filter implementation to install.
     * 
     * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
     */
    public class VelocityFilter implements Filter {
        VelocityRender renderer;
        
        public VelocityFilter(VelocityRender renderer) {
            this.renderer = renderer;
        }
        
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void destroy() {
        }
        
    }

}
