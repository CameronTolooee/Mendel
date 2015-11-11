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

package mendel.query;

import mendel.serialize.ByteSerializable;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a similarity query in Mendel for protein sequences.
 *
 * @author ctolooee
 */
public class SimilarityQuery implements ByteSerializable {

    private List<String> querySegments;
    private String querySequence;

    /**
     * Constructs a SimilarityQuery with a single sequence segment from a
     * base query.
     * @param segment the segment of the base query
     * @param sequence the base query sequence
     */
    public SimilarityQuery(String segment, String sequence) {
        this.querySegments = new ArrayList<>();
        this.querySegments.add(segment);
        this.querySequence = sequence;
    }

    /**
     * Constructs a SimilarityQuery containing multiple sequence segments
     * from a base query.
     * @param querySegments a list of query sequence segments
     * @param querySequence the base query sequence
     */
    public SimilarityQuery(List<String> querySegments, String querySequence) {
        this.querySegments = querySegments;
        this.querySequence = querySequence;
    }

    /**
     * Returns the list of a least one sequence segment for the query.
     * @return a list of the sequence segments
     */
    public List<String> getSequenceSegments() {
        return querySegments;
    }

    /**
     * Returns the whole base query of the SimilarityQuery.
     * @return the base query
     */
    public String getQuerySequence() {
        return querySequence;
    }

    @Deserialize
    public SimilarityQuery(SerializationInputStream in) throws IOException {
        querySegments = new ArrayList<>();
        int count = in.readInt();
        for (int i = 0; i < count; ++i) {
            querySegments.add(in.readString());
        }
        this.querySequence = in.readString();
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeInt(querySegments.size());
        for (String seq : querySegments) {
            out.writeString(seq);
        }
        out.writeString(querySequence);
    }

    @Override
    public String toString() {
        String sequences = "";
        for (String seq : querySegments) {
            sequences += seq + " ";
        }
        return sequences;
    }
}
