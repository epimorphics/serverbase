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
import com.epimorphics.vocabs.Cube;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.*;
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
    protected String rangeURI;
    protected Range range;
    protected boolean isOptional = false;
    protected boolean isMultiValued = false;
    protected String varname;

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
        
        analyzeRange(spec);
    }

    protected void analyzeRange(Resource spec) {
        Resource rangeR = RDFUtil.getResourceValue(spec, RDFS.range);
        if (rangeR != null) {
            rangeURI = rangeR.getURI();
        }
        if (spec.hasProperty(Cube.codeList) || (rangeR != null && (rangeR.equals(SKOS.Concept) || rangeR.hasProperty(RDFS.subClassOf, SKOS.Concept)))) {
            rangeCategory = RangeCategory.Hierarchy;
            // TODO work out hierarchy in use and create an API pointer for it
        } else {
            if (rangeR == null) {
                if (spec.hasProperty(RDF.type, OWL.DatatypeProperty)) {
                    rangeCategory = RangeCategory.Literal;
                } else {
                    rangeCategory = RangeCategory.Resource;
                }
                // TODO what to say about range values in this case
            } else {
                if ( rangeURI.startsWith(XSD.getURI()) ) {
                    rangeCategory = RangeCategory.Literal;
                    // TODO check for range declaration?
                } else {
                    rangeCategory = RangeCategory.Resource;
                    Model m = spec.getModel();
                    ResIterator ri = m.listSubjectsWithProperty(RDF.type, rangeR);
                    RangeOneof rnge = new RangeOneof();
                    ResourceCache cache = ResourceCache.get();
                    while (ri.hasNext()) {
                        rnge.addValue( cache.valueFromResource(ri.next()) );
                    }
                    range = rnge;
                }                
            }
        }
    }

    
    public String getVarname() {
        return varname;
    }

    public void setVarname(String varname) {
        this.varname = varname;
    }
    
    public String getId() {
        return id;
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.pair(ID, id);
        out.pair(LABEL, label);
        out.pair(DESCRIPTION, description);
        out.pair(ROLE, role.toString());
        out.pair(RANGE_CATEGORY, rangeCategory.toString());
        if (rangeURI != null) {
            out.pair(RANGE_URI, rangeURI);
        }
        if (range != null) {
            out.key(RANGE);
            range.writeTo(out);
        }
        out.pair(IS_OPTIONAL, isOptional);
        out.pair(IS_MULTIVALUED, isMultiValued);
        out.finishObject();
    }
}
