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

package mendel.vptree.types;

import mendel.serialize.SerializationInputStream;
import mendel.vptree.VPPoint;

import java.io.IOException;

public class ProteinSequence extends Sequence {

    public ProteinSequence(String word) {
        super(word);
    }

    public ProteinSequence(Sequence other) {
        super(other);
    }

    public ProteinSequence(VPPoint center) {
        super(center);
    }

    public ProteinSequence(SerializationInputStream in) throws IOException {
        super(in);
    }


    @Override
    public double getDistanceTo(VPPoint other) {
        String word2 = ((ProteinSequence) other).word;
        if (word == null || word2 == null) {
            throw new IllegalArgumentException("Received null argument");
        } else if (word.length() != word2.length()) {
            //throw new IllegalArgumentException("Kmers are not the same length");
        }
        word = word.toUpperCase();
        word2 = word2.toUpperCase();
        char[] c1 = word.toCharArray();
        char[] c2 = word2.toCharArray();
        int len = c1.length < c2.length ? c1.length : c2.length;
        int score = 0;
        for (int i = 0; i < len; i++) {
            Double val = SubMatrix.SUB_MATRIX.get(String.valueOf(c1[i]) + String.valueOf(c2[i]));
            if(val == null) {
                val = 13.0;
            }
            score += val;
        }
        return score;
    }



    public static void main(String[] args) {
        ProteinSequence seq1 = new ProteinSequence("MLDYFFNPKGIAVIGASNDPKKLGYEVFKNLKEYKKGKVYPVNIKEEEVQGVKAYKSVKD" +
                "IPDEIDLAIIVVPKRFVKDTLIQCGEKGVKGVVIITAGFGETGEEGKREEKELVEIAHKY" +
                "GMRIIGPNCVGIMNTHVDLNATFITVAKKGNVAFISQSGALGAGIVYKTIKEDIGFSKFI" +
                "SVGNMADVDFAELMEYLADTEEDKAIALYIEGVRNGKKFMEVAKRVTKKKPIIALKAGKS" +
                "ESGARAASSHTGSLAGSWKIYEAAFKQSGVLVANTIDEMLSMARAFSQP");

        ProteinSequence seq2 = new ProteinSequence("MSILIDKNTKVICQGFTGSQGTFHSEQAIAYGTKMVGGVTPGKGGTTHLGLPVFNTVREA" +
                "VAATGATASVIYVPAPFCKDSILEAIDAGIKLIITITEGIPTLDMLTVKVKLDEAGVRMI" +
                "GPNCPGVITPGECKIGIQPGHIHKPGKVGIVSRSGTLTYEAVKQTTDYGFGQSTCVGIGG" +
                "DPIPGSNFIDILEMFEKDPQTEAIVMIGEIGGSAEEEAAAYIKEHVTKPVVGYIAGVTAP" +
                "KGKRMGHAGAIIAGGKGTADEKFAALEAAGVKTVRSLADIGEALKTVLK");

        ProteinSequence seq3 = new ProteinSequence("MSILIDKNTKVICQGFTGSQGTFHSEQAIAYGTKMVGGVTPGKGGTTHLGLPVFNTVREAVAATGATASV" +
                "IYVPAPFCKDSILEAIDAGIKLIITITEGIPTLDMLTVKVKLDEAGVRMIGPNCPGVITPGECKIGIQPG" +
                "HIHKPGKVGIVSRSGTLTYEAVKQTTDYGFGQSTCVGIGGDPVPGSNFIDILEMFEKDPQTEAIVMIGEI" +
                "GGSAEEEAAAYIKEHVTKPVVGYIAGVTAPKGKRMGHAGAIIAGGKGTADEKFAALEAAGVKTVRSLADI" +
                "GEALKTVLK");

        System.out.println("1 -> 2: " + seq1.getDistanceTo(seq2));
        System.out.println("2 -> 1: " + seq2.getDistanceTo(seq1));
        System.out.println("1 -> 3: " + seq1.getDistanceTo(seq3));
        System.out.println("2 -> 3: " + seq2.getDistanceTo(seq3));

    }
}
