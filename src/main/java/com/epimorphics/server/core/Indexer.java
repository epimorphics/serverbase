/******************************************************************
 * File:        Indexer.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
