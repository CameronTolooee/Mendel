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

package mendel.tree;


/**
 * Implementation of a Kmer as a point in a {@link mendel.tree.VPTree}.
 *
 * @author ctolooee
 */
public class Kmer implements VPPoint {

    public String word;

    public Kmer(String word) {
        this.word = word;
    }

    @Override
    /**
     * Modified Hamming distance function implementation for contig reads.
     * Calculates the Hamming distance between <code>this</code> and the
     * parameter, taking wildcard characters into account.
     *
     * @param other the other {@link mendel.tree.Kmer} to compare the Hamming
     *              distance to.
     */
    public int distance(VPPoint other) {
        String word2;
        /* Validate arguments */
        if (other instanceof Kmer) {
            word2 = ((Kmer) other).word;
        } else {
            throw new IllegalArgumentException();
        }
        if (word == null || word2 == null
                || word.length() != word2.length()) {
            throw new IllegalArgumentException();
        }

        char[] c1 = word.toCharArray();
        char[] c2 = word2.toCharArray();
        int len = word.length(), count = len;

        for (int i = 0; i < len; i++) {
            /* Wildcard character 'N' always counts as a match */
            if (c1[i] == 'N' || c2[i] == 'N' || c1[i] == c2[i]) {
                --count;
            }
        }
        return count;
    }

    @Override
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
        return "Kmer{" +
                "word='" + word + '\'' +
                '}';
    }
}
