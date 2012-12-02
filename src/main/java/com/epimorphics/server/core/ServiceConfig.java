/******************************************************************
 * File:        Manager.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.server.indexers.LuceneIndex;
import com.epimorphics.server.templates.VelocityRender;
import com.epimorphics.util.EpiException;

/**
 * Controller object which interprets the server configuration and
 * instantiates the requested services. Supports a distinguished
 * service "store" for the default shared RDF dataset.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ServiceConfig implements ServletContextListener {
    static Logger log = LoggerFactory.getLogger(ServiceConfig.class);

    public static final String WEBAPP_MACRO = "${webapp}";
    public static final String CONFIG_PREFIX = "config.";
    public static final String CLASSNAME_PARAM = "class";

    public static final String STORE_SERVICENAME = "store";

    protected Map<String, Service> services = new HashMap<String, Service>();
    protected String filebase = null;

    protected Store defaultStore;

    public static ServiceConfig theConfig;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        theConfig = this;           // Keep the last initialized version as the default global config
        ServletContext context = sce.getServletContext();
        filebase =  withoutTrailingSlash(context.getRealPath("/"));

        Enumeration<String> paramNames = context.getInitParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = paramNames.nextElement();
            if (param.startsWith(CONFIG_PREFIX)) {
                String serviceName = param.substring(CONFIG_PREFIX.length());
                Service service = parseInit(serviceName, context.getInitParameter(param), context);
                services.put(serviceName, service);
            }
        }

        // Post-init pass to allow
        for (String serviceName : services.keySet()) {
            Service service = services.get(serviceName);
            if (service != null) {
                service.postInit();
            }
        }
        defaultStore = null;
    }

    public static ServiceConfig get() {
        return theConfig;
    }

    synchronized public List<String> getServiceNames() {
        return new ArrayList<String>( services.keySet() );
    }

    synchronized public Service getService(String name ) {
        return services.get(name);
    }

    public Store getDefaultStore() {
        if (defaultStore == null) {
            Service defaultStoreService = getService(STORE_SERVICENAME);
            if (defaultStoreService != null && defaultStoreService instanceof Store) {
                defaultStore = (Store) defaultStoreService;
            }
        }
        if (defaultStore == null) {
            throw new EpiException("No default store defined");
        }
        return defaultStore;
    }
    
    public LuceneIndex getDefaultIndex() {
        return getFirst(LuceneIndex.class);
    }

    public VelocityRender getDefaultRenderer() {
        return getFirst(VelocityRender.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T getFirst(Class<T> cls) {
        for (Service s : services.values()) {
            if (cls.isInstance(s)) {
                return (T)s;
            }
        }
        return null;
    }
    
    /**
     * Parse a component init string "class-name,p1=val1,p2=val2,..." and instantiate the service
     */
    private Service parseInit(String serviceName, String init, ServletContext context) {
        String[] segments = init.split(",");
        if (segments.length == 0) {
            throw new EpiException("No classname found for " + serviceName);
        }
        try {
            Service service = (Service) Class.forName( segments[0].trim() ).newInstance();
            Map<String, String> config = new HashMap<String, String>();
            for (int i = 1; i < segments.length; i++) {
                String param = segments[i].trim();
                String split[] = param.split("=");
                if (split.length != 2) {
                    throw new EpiException("Couldn't parse config parameter " + param);
                }
                config.put(split[0].trim(), split[1].trim());
            }
            service.init(config, context);
            return service;
        } catch (Exception e ) {
            throw new EpiException("Failed to instantiate service " + serviceName, e);
        }
    }

    public String expandFileLocation(String location) {
        return location.replace(WEBAPP_MACRO, filebase );
    }

    private String withoutTrailingSlash(String path) {
        return path.endsWith("/") ? path.substring(0, path.length()-1) : path;
    }
//
//    private String withTrailingSlash(String path) {
//        return path.endsWith("/") ? path : path + "/";
//    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // No action
    }

}
