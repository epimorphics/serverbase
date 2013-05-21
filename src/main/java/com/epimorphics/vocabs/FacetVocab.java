/* CVS $Id: $ */
package com.epimorphics.vocabs; 
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;
 
/**
 * Vocabulary definitions from src/main/vocabs/facet.ttl 
 * @author Auto-generated by schemagen on 16 Jun 2012 14:24 
 */
public class FacetVocab {
    /** <p>The ontology model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.epimorphics.com/ontologies/facet#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Indicates a list of filter Facet to be used in some context</p> */
    public static final ObjectProperty facets = m_model.createObjectProperty( "http://www.epimorphics.com/ontologies/facet#facets" );
    
    /** <p>The property or property path which links an item to a facet value, as a string 
     *  embedded in the overall query</p>
     */
    public static final DatatypeProperty query = m_model.createDatatypeProperty( "http://www.epimorphics.com/ontologies/facet#query" );
    
    /** <p>Specification for a single filter facet - needs a display label and a skos:notation 
     *  (which can be used as the variable name in queries)</p>
     */
    public static final OntClass Facet = m_model.createClass( "http://www.epimorphics.com/ontologies/facet#Facet" );
    
}