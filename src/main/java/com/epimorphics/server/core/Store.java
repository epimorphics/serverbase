/******************************************************************
 * File:        Store.java
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

import java.io.InputStream;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Simple RDF store abstraction which supports plug-in indexers triggered by
 * bulk updates. Is it up to the implementation how to store the graph units, normally
 * these will be named graphs within a DataSet.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Store {

    public void addGraph(String graphname, Model graph);
    public void addGraph(String graphname, InputStream input, String mimeType);

    public void updateGraph(String graphname, Model graph);
    public void updateGraph(String graphname, InputStream input, String mimeType);

    public void deleteGraph(String graphname);

    public void addIndexer(Indexer indexer);
    public void addMutator(Mutator mutator);

    public Dataset asDataset();

    public Model getUnionModel();

    public void lock();
    public void lockWrite();
    public void unlock();

}
