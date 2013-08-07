/******************************************************************
 * File:        Value.java
 * Created by:  Dave Reynolds
 * Created on:  7 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;

/**
 * Base class for values which a component can take. Can be used for reporting
 * property values in the JSON representation or setting filter values. 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class Value implements JSONWritable, Comparable<Value> {
    
    public abstract String getLexicalForm();
    
    @Override
    public abstract void writeTo(JSFullWriter out);
    
    @Override
    public abstract int compareTo(Value other);

}
