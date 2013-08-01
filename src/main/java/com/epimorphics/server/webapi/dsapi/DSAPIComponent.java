/******************************************************************
 * File:        DSAPIComponent.java
 * Created by:  Dave Reynolds
 * Created on:  1 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.server.webapi.DSAPIManager;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Represents the specification of a single component of a DSAPI.
 * A component is a property or pseudo property that is used in filters 
 * and returned as part of the presentation. In Data Cube terms it is a
 * dimension, a measure or an attribute.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DSAPIComponent {

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
    
    protected DSAPIManager manager;

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
    
    public DSAPIComponent(DSAPIManager manager, Resource spec) {
        this.manager = manager;
        this.spec = spec;
        
        // TODO parse spec 
    }
}
