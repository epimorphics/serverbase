/******************************************************************
 * File:        DSAPIManager.java
 * Created by:  Dave Reynolds
 * Created on:  1 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi;

import java.util.Map;

import com.epimorphics.server.webapi.dsapi.DSAPI;

/**
 * Makes a collection data-cube-like datasets available over a flexible
 * query API. May be generalized to remove the QB-specific notions.
 *  
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DSAPIManager {
    protected Map<String, DSAPI> datasets;
    
    
}
