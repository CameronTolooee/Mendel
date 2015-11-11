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

package mendel.vptree;


import mendel.serialize.ByteSerializable;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;
import mendel.util.SmithWaterman;

import java.io.IOException;

/**
 * Implementation of a Kmer as a point in a {@link mendel.vptree.VPTree}.
 *
 * @author ctolooee
 */
public class Kmer implements VPPoint, ByteSerializable {

    public String word, sequenceID;
    private int sequenceLength, sequencePos, length;


    public Kmer(String word) {
        this.word = word;
        this.length = word.length();
        this.sequenceID = "";
        this.sequencePos = -1;
        this.sequenceLength = -1;
    }

    public Kmer(Kmer other) {
        this.word = other.word;
        this.length = word.length();
        this.sequenceID = other.sequenceID;
        this.sequencePos = other.sequencePos;
        this.sequenceLength = other.sequenceLength;
    }

    public Kmer(VPPoint center) {
        if (!(center instanceof Kmer)) {
            throw new IllegalArgumentException();
        } else {
            Kmer other = (Kmer) center;
            this.word = other.word;
            this.length = word.length();
            this.sequenceID = other.sequenceID;
            this.sequencePos = other.sequencePos;
            this.sequenceLength = other.sequenceLength;
        }
    }

    @Override
    /**
     * Modified Hamming distance function implementation for config reads.
     * Calculates the Hamming distance between <code>this</code> and the
     * parameter, taking wildcard characters into account.
     *
     * @param other  the other {@link Kmer} to compare the Hamming
     *                 distance to.
     */
    public double getDistanceTo(VPPoint other) {
        String word2 = ((Kmer) other).word;
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

    @Override
    public int compareTo(VPPoint other) {
        String word2;

        /* Validate arguments */
        if (other instanceof Kmer) {
            word2 = ((Kmer) other).word;
        } else {
            throw new IllegalArgumentException();
        }
        return word.compareTo(word2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Kmer kmer = (Kmer) o;

        return word.equals(kmer.word);

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
    public Kmer(SerializationInputStream in) throws IOException {
        this.word = in.readString();
        this.sequenceID = in.readString();
        this.sequenceLength = in.readInt();
        this.sequencePos = in.readInt();
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeString(word);
        out.writeString(sequenceID);
        out.writeInt(sequenceLength);
        out.writeInt(sequencePos);
    }

    public String formatedOutput(Kmer query) {
        double score = 0;
        String output = "Sequence ID: " + sequenceID;
        output += "\tLength: " + sequenceLength + "\n";
        String line1 = "";
        String line2 = "\t\t";
        String line3 = "";
        line1 += "Query\t0\t";
        line3 += "Match\t" + this.sequencePos + "\t";
        SmithWaterman sw = new SmithWaterman();
        sw.init(query.word, this.word);
        sw.process();
        String seqA = sw.getmAlignmentSeqA();
        String seqB = sw.getmAlignmentSeqB();
        line1 += seqA;
        line3 += seqB;
        int count = 0;
        for (int i = 0, j = 0; i < seqA.length(); ++i, ++j) {
            if (seqA.charAt(i) == seqB.charAt(i)) {
                ++count;
                line2 += "|";
            } else {
                line2 += " ";
            }
        }
        output += "Score: " + sw.getmScore();
        output += "\t\tIdentities: " + count + "/" + seqA.length()
                + "(";
        output += String.format("%.2f", ((double) count / seqA.length()) * 100) + "%)\n";
        line1 += "\t" + (query.length - 1);
        line3 += "\t" + (this.sequencePos + query.word.length() - 1);
        return output + "\n" + line1 + "\n" + line2 + "\n" + line3;
    }

    public static void main(String[] args) {
        Kmer k1 = new Kmer("ACTGCACTGACTGCTGC");
        Kmer k2 = new Kmer("ACTGCACTGATTGCTGC");

        System.out.println(k1.formatedOutput(k2));

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

    public void setSequenceLength(int length) {
        sequenceLength = length;
    }

    public int getSequenceLength() {
        return sequenceLength;
    }
}
