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

package mendel.vptree.types;


import mendel.serialize.ByteSerializable;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;
import mendel.util.SmithWaterman;
import mendel.vptree.VPPoint;

import java.io.IOException;

/**
 * Implementation of a Sequence as a point in a {@link mendel.vptree.VPTree}.
 *
 * @author ctolooee
 */
public class Sequence implements VPPoint, ByteSerializable {

    protected String word, sequenceID, wholeSequece;
    protected int sequenceLength, sequencePos, length;


    public Sequence(String word) {
        this.word = word;
        this.length = word.length();
        this.sequenceID = "";
        this.sequencePos = -1;
        this.sequenceLength = -1;
        this.wholeSequece = "";
    }

    public Sequence(Sequence other) {
        this.word = other.word;
        this.length = word.length();
        this.sequenceID = other.sequenceID;
        this.sequencePos = other.sequencePos;
        this.sequenceLength = other.sequenceLength;
        this.wholeSequece = other.wholeSequece;
    }

    public Sequence(VPPoint center) {
        if (!(center instanceof Sequence)) {
            throw new IllegalArgumentException();
        } else {
            Sequence other = (Sequence) center;
            this.word = other.word;
            this.length = word.length();
            this.sequenceID = other.sequenceID;
            this.sequencePos = other.sequencePos;
            this.sequenceLength = other.sequenceLength;
            this.wholeSequece = other.wholeSequece;
        }
    }

    @Override
    /**
     * Modified Hamming distance function implementation for config reads.
     * Calculates the Hamming distance between <code>this</code> and the
     * parameter, taking wildcard characters into account.
     *
     * @param other  the other {@link Sequence} to compare the Hamming
     *                 distance to.
     */
    public double getDistanceTo(VPPoint other) {
        String word2 = ((Sequence) other).word;
        if (word == null || word2 == null) {
            throw new IllegalArgumentException("Received null argument");
        } else if (word.length() != word2.length()) {
            //throw new IllegalArgumentException("Kmers are not the same length");
        }

        char[] c1 = word.toCharArray();
        char[] c2 = word2.toCharArray();
        int len = c1.length < c2.length ? c1.length : c2.length;
        int count = len;

        for (int i = 0; i < len; i++) {
            /* Wildcard character 'N' always counts as a match */
            if (c1[i] == 'N' || c2[i] == 'N' || c1[i] == c2[i]) {
                --count;
            }
        }
        return count;
    }

    public boolean verifyMetricSpace() {
        //TODO
        return false;
    }

    public String formatedOutput(Sequence query) {
        return "";
    }


        @Override
    public int compareTo(VPPoint other) {
        String word2;

        /* Validate arguments */
        if (other instanceof Sequence) {
            word2 = ((Sequence) other).word;
        } else {
            throw new IllegalArgumentException();
        }
        return word.compareTo(word2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sequence sequence = (Sequence) o;

        return word.equals(sequence.word);

    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }

    @Override
    public String toString() {
        return word;
    }

    @Deserialize
    public Sequence(SerializationInputStream in) throws IOException {
        this.word = in.readString();
        this.sequenceID = in.readString();
        this.sequenceLength = in.readInt();
        this.sequencePos = in.readInt();
        this.wholeSequece = in.readString();
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeString(word);
        out.writeString(sequenceID);
        out.writeInt(sequenceLength);
        out.writeInt(sequencePos);
        out.writeString(wholeSequece);
    }

    public void setSequencePos(int sequencePos) {
        this.sequencePos = sequencePos;
    }

    public int getSequencePos() {
        return sequencePos;
    }

    public void setSequenceID(String sequenceID) {
        this.sequenceID = sequenceID;
    }

    public String getWord() {
        return word;
    }

    public String getSequenceID() {
        return sequenceID;
    }

    public void setSequenceLength(int length) {
        sequenceLength = length;
    }

    public int getSequenceLength() {
        return sequenceLength;
    }

    public int getLength() {
        return length;
    }

    public void setWholeSequence(String seq) {
        this.wholeSequece = seq;
    }

    public String getWholeSequece() {
        return wholeSequece;
    }
}
