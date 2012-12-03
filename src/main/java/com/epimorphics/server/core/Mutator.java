/******************************************************************
 * File:        Mutator.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.core;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Interface for services that can scan an uploaded model and modify them
 * (e.g. to compute an interface closure) prior to storage. Breaks streaming.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Mutator {

    public void mutate(Model data);
}
