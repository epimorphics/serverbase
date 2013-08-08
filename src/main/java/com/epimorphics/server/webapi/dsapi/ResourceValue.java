/******************************************************************
 * File:        ResourceValue.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.server.general.PrefixService;
import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Represents a value corresponding to an RDF Resource.
 * For the API each resource needs an identifying ID (which may be its
 * URI) and a label. Sorting is based on labels. If no ID is explicitly
 * given then will default to attempting to use any global PrefixService 
 * to create a shortname.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResourceValue extends Value {
    protected String uri;
    protected String id;
    protected String label;
    
    // TODO - should keep each lang label and be able to do query time lang-specific extraction?
    
    /**
     * Raw constructor.
     */
    public ResourceValue(String uri, String id, String label) {
        this.uri = uri;
        this.id = id;
        this.label = label;
    }

    /**
     * Constructor which consults the current values of a resource.
     * Looks for suitable name properties to generate the label and
     * skos:notation for the ID.
     * Caller must ensure that the associated model is suitably locked.
     */
    public ResourceValue(Resource r) {
        this.uri = r.getURI();
        this.label = RDFUtil.getLabel(r);
        this.id = RDFUtil.getStringValue(r, SKOS.notation);
    }

    /**
     * Constructor which consults the current values of a resource.
     * Looks for name values preferring the given language.
     * Caller must ensure that the associated model is suitably locked.
     */
    public ResourceValue(Resource r, String lang) {
        this.uri = r.getURI();
        this.label = RDFUtil.getLabel(r, lang);
        this.id = RDFUtil.getStringValue(r, SKOS.notation);
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        if (id == null) {
            PrefixService ps = PrefixService.get();
            if (ps != null) {
                id = ps.getPrefixes().shortForm(uri);
            } else {
                id = uri;
            }
        }
        return id;
    }

    public String getLabel() {
        return label;
    }
    
    public String getLexicalForm() {
        return label;
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.pair("@id", id);
        out.pair("uri", uri);
        out.pair("label", label);
        out.finishObject();
    }

    @Override
    public int compareTo(Value other) {
        return label.compareTo(other.getLexicalForm());
    }

    @Override
    public String asSPARQL() {
        return id;
    }
    
}
