/******************************************************************
 * File:        DSStructure.java
 * Created by:  Dave Reynolds
 * Created on:  8 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import static com.epimorphics.server.webapi.dsapi.JSONConstants.COMPONENTS;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.DESCRIPTION;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.ID;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.LABEL;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.URI;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.server.general.PrefixService;
import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.epimorphics.vocabs.Cube;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Represents the structure of a dataset. Normally extracted from a DSD.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO consider caching of DSDs so can structure share

public class DSStructure implements JSONWritable {
    protected String id;
    protected Resource dsd;
    protected String label;
    protected String description;

    protected List<DSAPIComponent> components = new ArrayList<>();
    
    /**
     * Parse a data structure definition to find the structure of a dataset.
     * The corresponding model must be suitably locked for reading.
     */
    public DSStructure(Resource dsd, DSAPI api) {
        this.dsd = dsd;
        this.id = PrefixService.get().getResourceID(dsd);
        this.label = RDFUtil.getLabel(dsd);
        this.description = RDFUtil.getDescription(dsd);
        extractComponents(dsd, api);
    }
    
    public List<DSAPIComponent> getComponents() {
        return components;
    }

    private void extractComponents(Resource dsd, DSAPI api) {
        for (Resource cspec : RDFUtil.allResourceValues(dsd, Cube.component)) {
            extractComponentsBy(cspec, Cube.measure, DSAPIComponent.ComponentRole.Measure, api);
            extractComponentsBy(cspec, Cube.dimension, DSAPIComponent.ComponentRole.Dimension, api);
            extractComponentsBy(cspec, Cube.attribute, DSAPIComponent.ComponentRole.Attribute, api);
            extractComponentsBy(cspec, Cube.componentProperty, null, api);
        }
    }
    
    private void extractComponentsBy(Resource cspec, Property prop, DSAPIComponent.ComponentRole role, DSAPI api) {
        for (Resource c : RDFUtil.allResourceValues(cspec, prop)) {
            DSAPIComponent component = new DSAPIComponent(c, role, api);
            // TODO extract isRequired flag
            components.add(component);
        }
        // TODO reorder based on any explicit ordering
        int count = 1;
        for (DSAPIComponent c : components) {
            c.setVarname("v" + count++);
        }
    }

    public String getId() {
        return id;
    }

    public Resource getDsd() {
        return dsd;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * Return the index of a component (identified by ID) within the structure signature,
     * or -1 if the component is not recognized
     */
    public int getComponentIndex(String id) {
        for (int i = 0; i < components.size(); i++) {
            if ( id.equals(components.get(i).getId()) ) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.pair(ID, id);
        out.pair(URI, dsd.getURI());
        out.pair(LABEL, label);
        out.pair(DESCRIPTION, description);
        out.key(COMPONENTS);
          out.startArray();
          for (DSAPIComponent c : getComponents()) {
              out.arrayElementProcess();
              c.writeTo(out);
          }
          out.finishArray();
        out.finishObject();
    }

}
