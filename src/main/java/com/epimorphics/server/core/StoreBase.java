/******************************************************************
 * File:        StoreBase.java
 * Created by:  Dave Reynolds
 * Created on:  27 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

/**
 * Base implementation of a generic store.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class StoreBase implements Store, Service {

    protected volatile List<Indexer> indexers = new ArrayList<Indexer>();
    protected boolean inWrite = false;

    @Override
    public void init(Map<String, String> config) {
        // No default initialisation
    }

    @Override
    public void postInit() {
        // TODO parse indexes from config
    }

    abstract void doAddGraph(String graphname, Model graph);
    abstract void doUpdateGraph(String graphname, Model graph);
    abstract void doDeleteGraph(String graphname);

    @Override
    public void addGraph(String graphname, Model graph) {
        for (Indexer i : indexers) {
            i.addGraph(graphname, graph);
        }
        doAddGraph(graphname, graph);
    }

    @Override
    public void updateGraph(String graphname, Model graph) {
        for (Indexer i : indexers) {
            i.updateGraph(graphname, graph);
        }
        doUpdateGraph(graphname, graph);
    }

    @Override
    public void deleteGraph(String graphname) {
        for (Indexer i : indexers) {
            i.deleteGraph(graphname);
        }
        doDeleteGraph(graphname);
    }

    @Override
    synchronized public void addIndexer(Indexer indexer) {
        List<Indexer> newIndexes = new ArrayList<Indexer>( indexers );
        newIndexes.add(indexer);
        // This should be and atomics switch of lists so no need to sync the methods that use the list
        indexers = newIndexes;
    }

    @Override
    abstract public Dataset asDataset();


    /** Lock the dataset for reading */
    public synchronized void lock() {
        Dataset dataset = asDataset();
        if (dataset.supportsTransactions()) {
            dataset.begin(ReadWrite.READ);
        } else {
            dataset.asDatasetGraph().getLock().enterCriticalSection(Lock.READ);
        }
    }

    /** Lock the dataset for write */
    public synchronized void lockWrite() {
        Dataset dataset = asDataset();
        if (dataset.supportsTransactions()) {
            dataset.begin(ReadWrite.WRITE);
            inWrite = true;
        } else {
            dataset.asDatasetGraph().getLock().enterCriticalSection(Lock.WRITE);
        }
    }

    /** Unlock the dataset */
    public synchronized void unlock() {
        Dataset dataset = asDataset();
        if (dataset.supportsTransactions()) {
            if (inWrite) {
                dataset.commit();
                inWrite = false;
            }
            dataset.end();
        } else {
            dataset.asDatasetGraph().getLock().leaveCriticalSection();
        }
    }

}
