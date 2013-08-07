/******************************************************************
 * File:        TypedValue.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Represents a value corresponding to a non-numeric typed literal.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TypedValue extends Value {
    protected Literal value;
    
    public TypedValue(Literal value) {
        this.value = value;
    }
    
    public Literal getValue() {
        return value;
    }

    @Override
    public String getLexicalForm() {
        return value.getLexicalForm();
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.pair("@type", value.getDatatypeURI());
        out.pair("@value", value.getLexicalForm());
        out.finishObject();
    }

    @Override
    public int compareTo(Value other) {
        // TODO handle things like dates with known orderings?
        return getLexicalForm().compareTo(other.getLexicalForm());
    }
}
