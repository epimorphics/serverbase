/******************************************************************
 * File:        MinimalRDFSClosure.java
 * Created by:  Dave Reynolds
 * Created on:  3 Dec 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.inference;

import java.util.Iterator;

import com.epimorphics.server.core.Mutator;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasoner;
import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Simple mutator which applies subclass and subproperty inference based
 * on a provided ontology.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RDFSMinClosure implements Mutator {
    OneToManyMap<Node, Node> subProperties = new OneToManyMap<Node, Node>();
    OneToManyMap<Node, Node> subClasses = new OneToManyMap<Node, Node>();

    public RDFSMinClosure(Model ontology) {
        InfModel closure = ModelFactory.createInfModel(new TransitiveReasoner(), ontology);
        extract(closure, RDFS.subPropertyOf, subProperties);
        extract(closure, RDFS.subClassOf, subClasses);
    }


    private void extract(Model closure, Property p, OneToManyMap<Node, Node> table) {
        StmtIterator si = closure.listStatements(null, p, (RDFNode)null);
        while (si.hasNext()) {
            Statement s = si.next();
            table.put(s.getSubject().asNode(), s.getObject().asNode());
        }
    }

    @Override
    public void mutate(Model dataM) {
        Graph deductions = Factory.createDefaultGraph();
        Graph data = dataM.getGraph();
        for (Node p : subProperties.keySet()) {
            ExtendedIterator<Triple> ti = data.find(Node.ANY, p, Node.ANY);
            while (ti.hasNext()) {
                Triple t = ti.next();
                for (Iterator<Node> i = subProperties.getAll(p); i.hasNext();) {
                    Node superP = i.next();
                    deductions.add( new Triple(t.getSubject(), superP, t.getObject()) );
                }
            }
        }
        for (Node c : subClasses.keySet()) {
            ExtendedIterator<Triple> ti = data.find(Node.ANY, RDF.type.asNode(), c);
            while (ti.hasNext()) {
                Triple t = ti.next();
                for (Iterator<Node> i = subClasses.getAll(c); i.hasNext();) {
                    Node superC = i.next();
                    deductions.add( new Triple(t.getSubject(), RDF.type.asNode(), superC) );
                }
            }
        }
        data.getBulkUpdateHandler().add(deductions);
    }

}
