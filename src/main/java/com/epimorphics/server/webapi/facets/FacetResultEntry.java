/******************************************************************
 * File:        FacetResultEntry.java
 * Created by:  Dave Reynolds
 * Created on:  14 Jun 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.facets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Package up a single result of a facet search. Comprises a found resource
 * together with a set of facet values.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class FacetResultEntry {

    public static final String ITEM_VAR = "item";

    RDFNode item;
    Map<String, Set<RDFNode>> metadata = new HashMap<String, Set<RDFNode>>();

    public FacetResultEntry(Map<String, RDFNode> result) {
        item = result.get(ITEM_VAR);
        for (Map.Entry<String, RDFNode> ent : result.entrySet()) {
            String key = ent.getKey();
            RDFNode value = ent.getValue();
            if (!key.equals(ITEM_VAR) && value != null) {
                Set<RDFNode> vals = new HashSet<RDFNode>();
                vals.add(value);
                metadata.put(key, vals);
            }
        }
    }

    public void mergeResult(Map<String, RDFNode> result) {
        if (result.get(ITEM_VAR).equals(item)) {
            for (Map.Entry<String, RDFNode> ent : result.entrySet()) {
                String key = ent.getKey();
                RDFNode value = ent.getValue();
                if (!key.equals(ITEM_VAR) && value != null) {
                    Set<RDFNode> vals = metadata.get(key);
                    vals.add(value);
                }
            }
        }
    }

    public RDFNode getItem() {
        return item;
    }

    public Map<String, Set<RDFNode>> getMetadata() {
        return metadata;
    }

}
