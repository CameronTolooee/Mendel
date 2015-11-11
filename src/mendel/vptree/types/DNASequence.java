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
import mendel.util.SmithWaterman;
import mendel.vptree.VPPoint;

import java.io.IOException;

public class DNASequence extends Sequence {

    public DNASequence(String word) {
        super(word);
    }

    public DNASequence(Sequence other) {
        super(other);
    }

    public DNASequence(VPPoint center) {
        super(center);
    }

    public DNASequence(SerializationInputStream in) throws IOException {
        super(in);
    }

    @Override
    public String formatedOutput(Sequence query) {
//        double score = 0;
//        String output = "Sequence ID: " + sequenceID;
//        output += "\tLength: " + sequenceLength + "\n";
//        String line1 = "";
//        String line2 = "\t\t";
//        String line3 = "";
//        line1 += "Query\t0\t";
//        line3 += "Match\t" + this.sequencePos + "\t";
//        SmithWaterman sw = new SmithWaterman();
//        sw.init(query.word, this.word);
//        sw.process();
//        String seqA = sw.getmAlignmentSeqA();
//        String seqB = sw.getmAlignmentSeqB();
//        line1 += seqA;
//        line3 += seqB;
//        int count = 0;
//        for (int i = 0, j = 0; i < seqA.length(); ++i, ++j) {
//            if (seqA.charAt(i) == seqB.charAt(i)) {
//                ++count;
//                line2 += "|";
//            } else {
//                line2 += " ";
//            }
//        }
//        output += "Score: " + sw.getmScore();
//        output += "\t\tIdentities: " + count + "/" + seqA.length()
//                + "(";
//        output += String.format("%.2f", ((double) count / seqA.length()) * 100) + "%)\n";
//        line1 += "\t" + (query.length - 1);
//        line3 += "\t" + (this.sequencePos + query.word.length() - 1);
//        return output + "\n" + line1 + "\n" + line2 + "\n" + line3;
        return "not yet implemented";
    }
}
