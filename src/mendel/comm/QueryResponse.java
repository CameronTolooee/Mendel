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

import mendel.event.Event;
import mendel.query.QueryResult;
import mendel.serialize.ByteSerializable;
import mendel.serialize.SerializationException;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines an internal event to respond to queries that have been processed
 * back to the client.
 *
 * @author ctolooee
 */
public class QueryResponse implements ByteSerializable, Event {

    List<QueryResult> response;
    String queryID, query;
    public long count; // FOR TESTING PURPOSES

    /**
     * Constructs a QueryResponse to the query with the specified ID.
     * Contains the original query and query ID, as well as the response. The
     * count parameter is used to get a segment count per node for testing
     * purposes.
     * @param response the list of QueryResults
     * @param queryID the base query's ID
     * @param count the number of segments on the storage node. This can be
     *              set to 0 if not needed.
     * @param query the base query
     */
    public QueryResponse(List<QueryResult> response, String queryID,
                         long count, String query) {
        this.response = response;
        this.queryID = queryID;
        this.count = count;
        this.query = query;
    }

    /**
     * Returns the base query's unique ID
     * @return the base query's ID
     */
    public String getQueryID() {
        return queryID;
    }

    /**
     * Returns the base query.
     * @return the base query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the list of QueryResults that were created during the
     * processing of the base query.
     * @return the list of QueryResults
     */
    public List<QueryResult> getResponse() {
        return response;
    }


    @Deserialize
    public QueryResponse(SerializationInputStream in)
            throws IOException, SerializationException {
        int size = in.readInt();
        response = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            response.add(new QueryResult(in));
        }
        queryID = in.readString();
        count = in.readLong();
        query = in.readString();
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeInt(response.size());
        for (QueryResult block : response) {
            out.writeSerializable(block);
        }
        out.writeString(queryID);
        out.writeLong(count);
        out.writeString(query);
    }
}
