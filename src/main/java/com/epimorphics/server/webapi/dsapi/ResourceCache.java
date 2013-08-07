/******************************************************************
 * File:        ResourceCache.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import org.apache.commons.collections.map.LRUMap;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.server.core.Service;
import com.epimorphics.server.core.ServiceBase;
import com.epimorphics.server.core.ServiceConfig;
import com.epimorphics.server.general.PrefixService;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Caches labels and IDs for resources to avoid having to run the queries each time.
 * Should be configured as a service with instances of a prefix service. The lookups are based
 * on the IDs which in turn are dependent upon the prefix service for stability.
 * Typically there will be only one ResourceCache in use and so a convenience method
 * to and return it is supplied.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO decide how to handle language coding of labels

// TODO change the label lookup so we just issue one SPARQL query and can do that against a SPARQL service instead of a store

// TODO change the API to allow for batch lookup for more efficient communication with the SPARQL endpoint, fine grain locking here smells

public class ResourceCache extends ServiceBase implements Service {
    protected static final String PREFIX_SERIVCE_PARAM = "prefixes";
    
    protected static final int MAX_SIZE = 10000;

    protected PrefixService prefixService;
    
    protected static ResourceCache rc;
    
    protected LRUMap cache = new LRUMap(MAX_SIZE);

    @Override
    public void postInit() {
        ServiceConfig sc = ServiceConfig.get();
        prefixService = sc.getServiceAs( getRequiredParam(PREFIX_SERIVCE_PARAM), PrefixService.class );
    }

    public static ResourceCache get() {
        if (rc == null) {
            rc = ServiceConfig.get().getFirst(ResourceCache.class);
        }
        return rc;
    }
    
    /**
     * Determine a shortname ID to use of the URI. 
     */
    public String idFor(Resource r) {
        return prefixService.getResourceID(r);
    }
    
    /**
     * Lookup a resource value from an ID and ensure it is in the given model (if model is not null).
     */
    public synchronized Resource resourceFromID(String id, Model model) {
        ResourceValue value = (ResourceValue) cache.get(id);
        if (value == null) {
            return reverseID(id, model);
        } else {
            if (model == null) {
                return ResourceFactory.createResource( value.getUri() );
            } else {
                return model.createResource( value.getUri() );
            }
        }
    }
    

    public synchronized ResourceValue valueFromResource(Resource r) {
        // TODO how to handle language coding here
        String label = RDFUtil.getLabel(r);
        String id = idFor(r);
        ResourceValue value = new ResourceValue(r.getURI(), id, label);
        cache.put(id, value);
        return value;
    }
    
    protected Resource reverseID(String id, Model model) {
        String uri = prefixService.getPrefixes().expandPrefix(id);
        if (model == null) {
            return ResourceFactory.createResource( uri );
        } else {
            return model.createResource( uri );
        }
    }

}
