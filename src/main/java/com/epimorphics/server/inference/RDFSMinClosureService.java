/******************************************************************
 * File:        RDFSMinClosureService.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2012
 * 
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.inference;

import java.util.Map;

import javax.servlet.ServletContext;

import com.epimorphics.server.core.Mutator;
import com.epimorphics.server.core.Service;
import com.epimorphics.server.core.ServiceBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;


/**
 * Simple mutator which applies subclass and subproperty inference based
 * on an ontology which is loaded from the file specified by configuration
 * parameter "ontology".
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RDFSMinClosureService extends ServiceBase implements Service, Mutator {
    public static final String ONTOLOGY_PARAM = "ontology";
    Mutator engine;
    
    @Override
    public void init(Map<String, String> config, ServletContext context) {
        this.config = config;
        String ontologyFile = getRequiredFileParam(ONTOLOGY_PARAM);
        Model ontology = FileManager.get().loadModel(ontologyFile);
        engine = new RDFSMinClosure(ontology);
    }

    @Override
    public void mutate(Model data) {
        engine.mutate(data);
    }

}
