/******************************************************************
 * File:        Indexer.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.core;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Interface for indexers which are notified when a store changes to update
 * their specialist indexes.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Indexer {

    /**
     * Signal an indexer that a batch of changes is starting, this may be
     * used to optimize commit cycles. No formal transaction semantics implied, is that needed here
     */
    public void startBatch();

    /**
     * Since the end up an update batch.
     */
    public void endBatch();

    public void addGraph(String graphname, Model graph);

    public void updateGraph(String graphname, Model graph);

    public void deleteGraph(String graphname);

}
