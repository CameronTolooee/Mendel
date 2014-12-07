/*
 * Copyright (c) 2014, Colorado State University All rights reserved.
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
import mendel.fs.Block;
import mendel.serialize.ByteSerializable;
import mendel.serialize.SerializationException;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueryResponse implements ByteSerializable, Event {

    List<Block> response;
    String queryID;

    public QueryResponse(List<Block> response, String queryID) {
        this.response = response;
        this.queryID = queryID;

    }

    public String getQueryID() {
        return queryID;
    }

    public List<Block> getResponse() {
        return response;
    }


    @Deserialize
    public QueryResponse(SerializationInputStream in)
            throws IOException, SerializationException {
        int size = in.readInt();
        response = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            response.add(new Block(in));
        }
        queryID = in.readString();
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeInt(response.size());
        for (Block block : response) {
            out.writeSerializable(block);
        }
        out.writeString(queryID);
    }
}
