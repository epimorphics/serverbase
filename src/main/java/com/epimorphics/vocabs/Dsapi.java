/* CVS $Id: $ */
package com.epimorphics.vocabs; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from src/main/vocabs/dsapi.ttl 
 * @author Auto-generated by schemagen on 09 Aug 2013 13:20 
 */
public class Dsapi {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.epimorphics.com/public/vocabulary/dsapi#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Lowest value expected for a measure or other cube component</p> */
    public static final Property lowerBound = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#lowerBound" );
    
    /** <p>Hiest value expected for a measure or other cube component</p> */
    public static final Property upperBound = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#upperBound" );
    
}
