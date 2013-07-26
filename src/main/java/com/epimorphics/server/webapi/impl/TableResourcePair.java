/******************************************************************
 * File:        TableResourcePair.java
 * Created by:  Dave Reynolds
 * Created on:  26 Jul 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.impl;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simple structure to represent a resource name/URI pair.
 * Typically these will both be extracted by a query
 * and then merged into a single JSON object for 
 * rendering as a link in the Datatable column.
 * <p>
 * N.B. This approach will fail if there are multiple labels,
 * or rather there will be multiple rows in the table.
 * </p>
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
@XmlRootElement
public class TableResourcePair implements Comparable<TableResourcePair> {

    protected String name;
    protected String uri;
    
    public TableResourcePair() {
    }

    public TableResourcePair(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
    @Override
    public String toString() {
        // Filtering should just check the name
        return name;
    }

    @Override
    public int compareTo(TableResourcePair o) {
        return name.compareTo(o.getName());
    }
}
