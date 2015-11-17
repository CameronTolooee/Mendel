/*
 * Copyright (c) 2015, Colorado State University All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * This software is provided by the copyright holders and contributors "as is"
 * and any express or implied warranties, including, but not limited to, the
 * implied warranties of merchantability and fitness for a particular purpose
 * are disclaimed. In no event shall the copyright holder or contributors be
 * liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of
 * substitute goods or services; loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in
 * contract, strict liability, or tort (including negligence or otherwise)
 * arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 */

package mendel.comm;

import java.io.IOException;

import mendel.event.Event;
import mendel.query.SimilarityQuery;
import mendel.serialize.SerializationException;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;

/**
 * Defines an internal event to signal to forward queries to the correct
 * {@link mendel.dht.StorageNode}s for processing. This is functionally the
 * same as a {@link mendel.comm.QueryEvent}, but is processed differently
 * upon arrival and thus is its own entity.
 *
 * @author ctolooee
 */
public class QueryRequest implements Event {

    private SimilarityQuery query;
    private String queryID;

    /**
     * Constructs a QueryRequest with the given SimilarityQuery and ID.
     * @param query the SimilarityQuery to be processed
     * @param queryID the unique global id for this query
     */
    public QueryRequest(SimilarityQuery query, String queryID) {
        this.query = query;
        this.queryID = queryID;
    }

    /**
     * Returns the SimilarityQuery that describes this Event.
     * @return the SimilarityQuery contained in the Event
     */
    public SimilarityQuery getQuery() {
        return query;
    }

    /**
     * Returns the query's unique id string. This can be used to track
     * queries throughout the system.
     * @return the query's unique ID
     */
    public String getQueryID() {
        return queryID;
    }

    @Deserialize
    public QueryRequest(SerializationInputStream in)
            throws IOException, SerializationException {
        query = new SimilarityQuery(in);
        queryID = in.readString();
    }

    @Override
    public void serialize(SerializationOutputStream out)
            throws IOException {
        out.writeSerializable(query);
        out.writeString(queryID);
    }
}