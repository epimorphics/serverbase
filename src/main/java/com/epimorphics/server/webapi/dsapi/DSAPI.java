/******************************************************************
 * File:        DSAPI.java
 * Created by:  Dave Reynolds
 * Created on:  1 Aug 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.server.webapi.dsapi;

import static com.epimorphics.server.webapi.dsapi.JSONConstants.DATA_API;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.DESCRIPTION;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.ID;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.LABEL;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.STRUCTURE_API;
import static com.epimorphics.server.webapi.dsapi.JSONConstants.URI;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.server.core.Store;
import com.epimorphics.server.general.PrefixService;
import com.epimorphics.server.webapi.DSAPIManager;
import com.epimorphics.server.webapi.WebApiException;
import com.epimorphics.server.webapi.marshalling.JSFullWriter;
import com.epimorphics.server.webapi.marshalling.JSONWritable;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Represents a single configured dataset API.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// TODO handle language settings - different view for each language or on demand lookup? 

public class DSAPI implements JSONWritable {
    static Logger log = LoggerFactory.getLogger(DSAPI.class);
    
    protected DSAPIManager man;

    protected String id;
    protected Resource dataset;
    protected String label;
    protected String description;
    protected DSStructure structure;
    
    public DSAPI(DSAPIManager man, Resource dataset, Resource dsd) {
        this.man = man;
        this.dataset = dataset;
        this.id = PrefixService.get().getResourceID(dataset);
        this.label = RDFUtil.getLabel(dataset);
        this.description = RDFUtil.getDescription(dataset);

        structure = new DSStructure(dsd);
    }
    
    public String getURI() {
        return dataset.getURI();
    }
    
    public List<DSAPIComponent> getComponents() {
        return structure.getComponents();
    }
    
    /**
     * Return the index of a component (identified by ID) within the structure signature,
     * or -1 if the component is not recognized
     */
    public int getComponentIndex(String id) {
        return structure.getComponentIndex(id);
    }
     
    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        out.pair(ID, id);
        out.pair(URI, getURI());
        out.pair(LABEL, label);
        out.pair(DESCRIPTION, description);
        out.pair(DATA_API, man.getApiBase() + "/" + id + "/data");
        out.pair(STRUCTURE_API, man.getApiBase() + "/" + id + "/structure");
        out.finishObject();
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    
    
    public DSStructure getStructure() {
        return structure;
    }

    /**
     * Takes a request state as a JSON description, runs the corresponding query
     * and returns the correct slice of the results table as a JSON wriable object.
     */
    public Projection project(JsonObject jstate) {
        State state = null;
        try {
            state = new State(jstate);
        } catch (Exception e) {
            throw new WebApiException(Status.BAD_REQUEST, e.getMessage());
        }
        for (String key : state.keySet()) {
            if ( (!key.startsWith("_")) && getComponentIndex(key) == -1) {
                throw new WebApiException(Status.BAD_REQUEST, "Filter requested on unrecognized component: " + key);
            }
        }
        
        // TODO caching
        
        Projection projection = queryData(state, man.getStore());
        if (state.hasKey(State.SORT_PARAM)) {
            projection = projection.sort(state.getString(State.SORT_PARAM));
        }
        if (state.hasKey(State.OFFSET_PARAM) || state.hasKey(State.LIMIT_PARAM)) {
            projection = projection.slice( state.getInt(State.OFFSET_PARAM, 0), state.getInt(State.LIMIT_PARAM, Integer.MAX_VALUE));
        }
        return projection;
    }
    
    public Projection queryData(State state, Store store) {
        SPARQLFilterQuery query = new SPARQLFilterQuery();
        for (DSAPIComponent c : getComponents()) {
            Range r = state.getRangeFor(c);
            if (r == null) {
                query.addQuery(c);
            } else {
                query.addFilter(c, r);
            }
        }
        
        // TODO sort in the SPARQL query?

        store.lock();
        String qstr = query.getQuery();
        log.debug("Project query is: " + qstr);
        QueryExecution qexec = QueryExecutionFactory.create(qstr, store.getUnionModel());
        try {
            ResultSetRewindable rs = ResultSetFactory.copyResults( qexec.execSelect() );
            return new Projection(this, parseResults(rs, state) );
        } finally {
            qexec.close();
            store.unlock();
        }
    }
    
    /**
     * Convert a SPARQL result set to a flat data array.
     */
    // TODO version that dynamically grows Object so it doesn't need a rewindable result set
    // TODO version that handles coalescing of attributes
    public Object[][] parseResults(ResultSetRewindable rs, State state) {
        ResourceCache rc = ResourceCache.get();
        Object[][] data = new Object[rs.size()][];
        int rowlen = getComponents().size();
        int index = 0;
        while(rs.hasNext()) {
            QuerySolution soln = rs.next();
            Object[] row = new Object[rowlen];
            int rindex = 0;
            for (DSAPIComponent c : getComponents()) {
                Value v = null;
                RDFNode n = soln.get( c.getVarname() );
                if (n != null) {
                    v = rc.valueFromNode(n);
                } else {
                    Range r = state.getRangeFor(c);
                    if (r != null && r instanceof RangeOneof) {
                        v = ((RangeOneof)r).getSingletonValue();
                    }
                }
                // TODO how to recover if we have not v value at this point?
                row[rindex++] = v;
            }
            data[index++] = row;
        }
        return data;
    }

}
