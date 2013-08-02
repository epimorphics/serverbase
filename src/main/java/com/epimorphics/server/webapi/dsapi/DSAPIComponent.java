/******************************************************************
 * File:        DSAPIComponent.java
 * Created by:  Dave Reynolds
 * Created on:  1 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.server.general.PrefixService;
import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Represents the specification of a single component of a DSAPI.
 * A component is a property or pseudo property that is used in filters 
 * and returned as part of the presentation. In Data Cube terms it is a
 * dimension, a measure or an attribute.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DSAPIComponent implements JSONWritable {

    public enum ComponentRole {
        Dimension,
        Measure,
        Attribute
    }
    
    public enum RangeCategory {
        Literal,
        Resource,
        Hierarchy
    }
    
    protected DSAPI api;

    protected Resource spec;

    protected String id;
    protected String label;
    protected String description;
    protected ComponentRole role;
    protected RangeCategory rangeCategory;
    protected String rangeType;
    boolean isOptional = false;
    boolean isMultiValued = false;

    // Hierarchy - need to represent hierarchy in a way that enables us to query for levels and roots
    
    // Range
    
    public DSAPIComponent(DSAPI api, Resource spec) {
        this(api, spec, null);
    }
    
    public DSAPIComponent(DSAPI api, Resource spec, ComponentRole role) {
        this.api = api;
        this.spec = spec;
        this.role = role;
        
        this.id = PrefixService.get().getResourceID(spec);
        this.label = RDFUtil.getLabel(spec);
        this.description = RDFUtil.getDescription(spec);
        
        // TODO parse spec 
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.pair("id", id);
        out.pair("label", label);
        out.pair("description", description);
        out.pair("role", role.toString());
        out.pair("isOptional", isOptional);
        out.pair("isMultiValued", isMultiValued);
        out.finishObject();
    }
}
