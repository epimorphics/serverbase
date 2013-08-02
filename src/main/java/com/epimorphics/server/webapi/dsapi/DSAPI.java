/******************************************************************
 * File:        DSAPI.java
 * Created by:  Dave Reynolds
 * Created on:  1 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.epimorphics.vocabs.Cube;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Represents a single configured dataset API.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO handle language settings - different view for each language or on demand lookup? 

public class DSAPI implements JSONWritable {
    protected String id;
    protected String label;
    protected String description;

    protected Resource dsd;
    
    protected List<DSAPIComponent> components = new ArrayList<>();
    
    public DSAPI(Resource dsd, String id) {
        this.dsd = dsd;
        this.id = id;
        this.label = RDFUtil.getLabel(dsd);
        this.description = RDFUtil.getDescription(dsd);
        extractComponents();
    }
    
    public List<DSAPIComponent> getComponents() {
        return components;
    }

    private void extractComponents() {
        for (Resource cspec : RDFUtil.allResourceValues(dsd, Cube.component)) {
            extractComponentsBy(cspec, Cube.measure, DSAPIComponent.ComponentRole.Measure);
            extractComponentsBy(cspec, Cube.dimension, DSAPIComponent.ComponentRole.Dimension);
            extractComponentsBy(cspec, Cube.attribute, DSAPIComponent.ComponentRole.Attribute);
            extractComponentsBy(cspec, Cube.componentProperty, null);
        }
    }
    
    private void extractComponentsBy(Resource cspec, Property prop, DSAPIComponent.ComponentRole role) {
        for (Resource c : RDFUtil.allResourceValues(cspec, prop)) {
            DSAPIComponent component = new DSAPIComponent(this, c, role);
            // TODO extract isRequired flag
            components.add(component);
        }
        // TODO reorder based on any explicit ordering
    }
    
    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.pair("id", id);
        out.pair("label", label);
        out.pair("description", description);
        out.key("components");
          out.startArray();
          for (DSAPIComponent c : getComponents()) {
              out.arrayElementProcess();
              c.writeTo(out);
          }
          out.finishArray();
        out.finishObject();
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

}
