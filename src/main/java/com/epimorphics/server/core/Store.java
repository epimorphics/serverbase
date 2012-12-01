/******************************************************************
 * File:        Store.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
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

    public Dataset asDataset();

    public void lock();
    public void lockWrite();
    public void unlock();

}
