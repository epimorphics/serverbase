/******************************************************************
 * File:        DSAPI.java
 * Created by:  Dave Reynolds
 * Created on:  1 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import static com.epimorphics.server.webapi.dsapi.JSONConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.server.core.Store;
import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.epimorphics.vocabs.Cube;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Represents a single configured dataset API.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO handle language settings - different view for each language or on demand lookup? 

public class DSAPI implements JSONWritable {
    protected String id;
    protected Resource dataset;
    protected String label;
    protected String description;

    protected List<DSAPIComponent> components = new ArrayList<>();
    
    public DSAPI(Resource dataset, Resource dsd, String id) {
        this.dataset = dataset;
        this.id = id;
        this.label = RDFUtil.getLabel(dataset);
        this.description = RDFUtil.getDescription(dataset);
        extractComponents(dsd);
    }
    
    public List<DSAPIComponent> getComponents() {
        return components;
    }

    private void extractComponents(Resource dsd) {
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
        int count = 1;
        for (DSAPIComponent c : components) {
            c.setVarname("v" + count++);
        }
    }
    
    public String getURI() {
        return dataset.getURI();
    }
    
    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.pair(ID, id);
        out.pair(URI, getURI());
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

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
    
    public Projection queryData(State state, Store store) {
        SPARQLFilterQuery query = new SPARQLFilterQuery();
        for (DSAPIComponent c : components) {
            Range r = state.getRangeFor(c);
            if (r == null) {
                query.addQuery(c);
            } else {
                query.addFilter(c, r);
            }
        }

        store.lock();
        QueryExecution qexec = QueryExecutionFactory.create(query.getQuery(), store.getUnionModel());
        try {
            ResultSetRewindable rs = ResultSetFactory.copyResults( qexec.execSelect() );
            return new Projection( parseResults(rs, state) );
        } finally {
            qexec.close();
            store.unlock();
        }
    }
    
    /**
     * Convert a SPARQL result set to a flat data array.
     */
    // TODO version that dynamically grows Object so it doesn't need a rewindable result set
    // TODO version that handles coalescing of attributes
    public Object[][] parseResults(ResultSetRewindable rs, State state) {
        ResourceCache rc = ResourceCache.get();
        Object[][] data = new Object[rs.size()][];
        int rowlen = components.size();
        int index = 0;
        while(rs.hasNext()) {
            QuerySolution soln = rs.next();
            Object[] row = new Object[rowlen];
            int rindex = 0;
            for (DSAPIComponent c : components) {
                Value v = null;
                RDFNode n = soln.get( c.getVarname() );
                if (n != null) {
                    v = rc.valueFromNode(n);
                } else {
                    Range r = state.getRangeFor(c);
                    if (r != null && r instanceof RangeOneof) {
                        v = ((RangeOneof)r).getSingletonValue();
                    }
                }
                // TODO how to recover if we have not v value at this point?
                row[rindex++] = v;
            }
            data[index++] = row;
        }
        return data;
    }
  
}
