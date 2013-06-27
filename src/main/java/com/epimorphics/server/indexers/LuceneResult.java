/******************************************************************
 * File:        LuceneResult.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2012
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

package com.epimorphics.server.indexers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * A lucence search result entry. Provides the match score, the entity URI, the name
 * of the graph it was registered in and the value of any indexed&stored
 * properties (as strings).
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class LuceneResult {

    Document doc;
    float score;

    public LuceneResult(Document doc, float score) {
        this.doc = doc;
        this.score = score;
    }

    public float getScore() {
        return score;
    }

    public String getURI() {
        return doc.get(LuceneIndex.FIELD_URI);
    }

    public String getGraphname() {
        return doc.get(LuceneIndex.FIELD_GRAPH);
    }


    public List<String> fieldNames() {
        Set<String> found = new HashSet<String>();
        List<String> fieldNames = new ArrayList<String>();
        for (IndexableField field : doc.getFields()) {
            String name = field.name();
            if (found.add(name)) {
                fieldNames.add(name);
            }
        }
        return fieldNames;
    }

    /**
     * Returns all the values of a field. These will be either Strings (for literals and labels),
     * Resources (for URI fields) or Longs (for numeric fields)
     */
    public Object[] fieldValues(String fieldName) {
        IndexableField[] fields = doc.getFields(fieldName);
        Object[] results = new Object[ fields.length ];
        for (int i = 0; i < fields.length; i++) {
            IndexableField field = fields[i];
            Object value = field.numericValue();
            if (value == null) {
                value = field.stringValue();
            }
            if (value == null) {
                BytesRef ref = field.binaryValue();
                value = ResourceFactory.createResource( ref.utf8ToString() );
            }
            results[i] = value;
        }
        return results;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append( String.format("Result(%f): %s in <%s>", getScore(), getURI(), getGraphname()) );
        return buff.toString();
    }

}
