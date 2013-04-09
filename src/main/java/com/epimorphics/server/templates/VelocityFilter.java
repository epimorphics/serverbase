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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.util.EpiException;

    /**
     * The filter implementation for driving the VelocityRenderer
     *
     * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
     */
    public class VelocityFilter implements Filter {
        public static final String SERIVCE_NAME_PARAM    = "renderService";

        VelocityRender renderer;
        String rendererServiceName;

        public VelocityFilter() {
        }

        public VelocityFilter(VelocityRender renderer) {
            this.renderer = renderer;
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            rendererServiceName = filterConfig.getInitParameter(SERIVCE_NAME_PARAM);
        }

        protected VelocityRender getRenderer() {
            if (renderer == null){
                if (rendererServiceName != null) {
                    renderer = ServiceConfig.get().getServiceAs(rendererServiceName, VelocityRender.class);
                } else {
                    throw new EpiException("Velocity filter not configured with renderer service");
                }
            }
            return renderer;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                HttpServletRequest hrequest = (HttpServletRequest) request;
                HttpServletResponse hresponse = (HttpServletResponse) response;
                if (getRenderer().render(hrequest, hresponse, null)) {
                    return;
                }
            }
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
        }

    }

