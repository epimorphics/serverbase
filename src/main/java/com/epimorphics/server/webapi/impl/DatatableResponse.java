/******************************************************************
 * File:        DatatableResponse.java
 * Created by:  Dave Reynolds
 * Created on:  24 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.impl;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

/**
 * Format of a response to the jQuery datatypes plugin.
 * Assumes jaxson POJO to JSON conversion
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
@XmlRootElement
public class DatatableResponse {

    protected int iTotalRecords;
    protected int iTotalDisplayRecords;
    protected String sEcho;
    protected Object[][] aaData;
    
    public DatatableResponse() {
    }
    
    public int getiTotalRecords() {
        return iTotalRecords;
    }
    public void setiTotalRecords(int iTotalRecords) {
        this.iTotalRecords = iTotalRecords;
    }
    public int getiTotalDisplayRecords() {
        return iTotalDisplayRecords;
    }
    public void setiTotalDisplayRecords(int iTotalDisplayRecords) {
        this.iTotalDisplayRecords = iTotalDisplayRecords;
    }
    public String getsEcho() {
        return sEcho;
    }
    public void setsEcho(String sEcho) {
        this.sEcho = sEcho;
    }
    public Object[][] getAaData() {
        return aaData;
    }
    public void setAaData(Object[][] aaData) {
        this.aaData = aaData;
    }
    
    
    /**
     * Can't get jackson pojo support working? Use this low level serialization
     */
    JsonValue asJson() {
        JsonObject top = new JsonObject();
        top.put("iTotalRecords", iTotalRecords);
        top.put("iTotalDisplayRecords", iTotalDisplayRecords);
        top.put("sEcho", sEcho);
        JsonArray data = new JsonArray();
        for (int i = 0; i < aaData.length; i++) {
            Object[] row = aaData[i];
            JsonArray dataRow = new JsonArray();
            for (Object val : row) {
                if (val instanceof Number) {
                    dataRow.add( ((Number)val).longValue() );
                } else if (val instanceof String) {
                    dataRow.add( (String)val);
                } else {
                    dataRow.add( val.toString() );
                }
            }
            data.add( dataRow );
        }
        top.put("aaData", data);
        return top;
    }
    
    
}
