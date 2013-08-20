/******************************************************************
 * File:        RangeHierarchy.java
 * Created by:  Dave Reynolds
 * Created on:  16 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import static com.epimorphics.server.webapi.dsapi.JSONConstants.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.epimorphics.server.webapi.marshalling.JSFullWriter;

/**
 * Represents a hierarchy filter query which can select members based on being
 * below one or more concepts in the tree, and/or members of a given collection.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RangeHierarchy extends Range {
    protected List<ResourceValue> parents = new ArrayList<>();
    protected List<ResourceValue> collections = new ArrayList<>();
    
    public RangeHierarchy() {
    }
    
    public void addParent(ResourceValue parent) {
        parents.add(parent);
    }
    
    public void addCollection(ResourceValue collection) {
        collections.add( collection );
    }
    
    public String filterQuery(DSAPIComponent c) {
        StringBuffer query = new StringBuffer();
        
        // Base query for dimension of the observation
        query.append( String.format("%s %s ?%s.",  SPARQLFilterQuery.OBS_VAR, c.asSPARQL(), c.getVarname()) );
        
        // Any root term filters
        if (parents.size() == 1) {
            query.append( String.format("?%s skos:broader * <%s>.",  c.getVarname(), parents.get(0).getUri()) );

        } else if (parents.size() > 1) {
            query.append("{");
            for (Iterator<ResourceValue> i = parents.iterator(); i.hasNext();) {
                query.append( String.format("{?%s skos:broader * <%s>.}",  c.getVarname(), i.next().getUri()) );
                if (i.hasNext()) {
                    query.append(" UNION ");
                }
            }
            query.append("}");
        }
        
        // Any collection filters
        if (collections.size() == 1) {
            query.append( String.format("<%s> skos:member ?%s .",  collections.get(0).getUri(), c.getVarname()) );

        } else if (collections.size() > 1) {
            query.append("{");
            for (Iterator<ResourceValue> i = collections.iterator(); i.hasNext();) {
                query.append( String.format("{<%s> skos:member ?%s .}",  i.next().getUri(), c.getVarname()) );
                if (i.hasNext()) {
                    query.append(" UNION ");
                }
            }
            query.append("}");
        }

        return query.toString();
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        writeArray(BELOW, parents, out);
        writeArray(IN_COLLECTION, collections, out);
        out.finishObject();
    }

    private void writeArray(String key, List<ResourceValue> arr, JSFullWriter out) {
        if (!arr.isEmpty()) {
            out.key(key);
            out.startArray();
            for (Iterator<ResourceValue> i = arr.iterator(); i.hasNext();) {
                i.next().writeTo(out);
                if (i.hasNext()) out.arraySep();
            }
            out.finishArray();
        }
    }
}
