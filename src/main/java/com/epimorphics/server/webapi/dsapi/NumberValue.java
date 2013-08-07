/******************************************************************
 * File:        NumberValue.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import java.math.BigDecimal;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * A numeric value.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class NumberValue extends Value {
    // Brute force implementation that makes everything a BigDecimal

    protected BigDecimal value;
    
    public NumberValue(long i) {
        value = new BigDecimal(i);
    }
    
    public NumberValue(double d) {
        value = new BigDecimal(d);
    }
    
    public NumberValue(Literal l) {
        Object val = l.getValue();
        if (val instanceof Long) {
            value = new BigDecimal( ((Long)val).longValue() );
        } else  if (val instanceof Double) {
            value = new BigDecimal( ((Double)val).doubleValue() );
        } else  if (val instanceof BigDecimal) {
            value = (BigDecimal)val;
        }
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public long asLong() {
        return value.longValueExact();
    }
    
    public double asDouble() {
        return value.doubleValue();
    }

    @Override
    public String getLexicalForm() {
        return value.toString();
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.print( value.toString() );
    }

    @Override
    public int compareTo(Value other) {
        if (other instanceof NumberValue) {
            return value.compareTo( ((NumberValue)other).value );
        } else {
            return getLexicalForm().compareTo(other.getLexicalForm());
        }
    }
    
}
