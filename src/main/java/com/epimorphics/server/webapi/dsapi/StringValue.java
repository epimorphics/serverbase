/******************************************************************
 * File:        StringValue.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;

/**
 * Package a plain string as a value in a value range.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class StringValue extends Value{
    protected String lex;
    
    public StringValue(String lex) {
        this.lex = lex;
    }

    public String getValue() {
        return lex;
    }

    @Override
    public String getLexicalForm() {
        return lex;
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.print(lex);
    }

    @Override
    public int compareTo(Value other) {
        return lex.compareTo(other.getLexicalForm());
    }
    
}
