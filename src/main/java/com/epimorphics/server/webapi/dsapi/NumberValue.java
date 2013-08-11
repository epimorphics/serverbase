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
import java.math.MathContext;

import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.util.EpiException;
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
        value = value.round(MathContext.DECIMAL64);
    }
    
    public NumberValue(BigDecimal d) {
        value = d;
    }
    
    public NumberValue(Number val) {
        setFrom(val);
    }

    public NumberValue(JsonValue val) {
        if (val.isNumber()) {
            setFrom( val.getAsNumber().value() );
        } else {
            throw new EpiException("Illegal number: " + val);
        }
    }
    
    private void setFrom(Number val) {
        if (val instanceof Double || val instanceof Float) {
            value = new BigDecimal( ((Double)val).doubleValue() );
            value = value.round(MathContext.DECIMAL64);
        } else if (val instanceof BigDecimal) {
            value = (BigDecimal)val;
        } else {
            value = new BigDecimal( ((Number)val).longValue() );
        }
    }
    
    public NumberValue(Literal l) {
        Object val = l.getValue();
        if (l instanceof Number) {
            setFrom((Number)val);
        } else {
            throw new EpiException("Literal is not a number: " + l);
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

    @Override
    public String asSPARQL() {
        return value.toString();
    }
    
}
