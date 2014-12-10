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

package mendel.dht.hash;

import mendel.data.Metadata;
import mendel.vptree.Kmer;
import mendel.vptree.VPTree;

import java.math.BigInteger;
import java.util.Random;

public class VPHashTree implements HashFunction<Metadata> {

    private Random random = new Random();
    private VPTree<Kmer> tree;

    public VPHashTree() {
        this.tree = new VPTree<>();
    }


    public VPHashTree(VPTree<Kmer> tree) {
        this.tree = tree;
    }

    public long lookup(Kmer value) throws HashException {
        long retval = tree.getPrefixOf(value);
        if (retval < 0) {
            if((retval = tree.add(value)) < 0) {
                throw new HashException("Cannot add " + value + "to VPTree");
            }
        }
        return retval;
    }

    @Override
    public BigInteger hash(Metadata metadata) throws HashException {
        Kmer data = new Kmer(metadata.getSeqBlock());
        return BigInteger.valueOf(lookup(data));
    }

    @Override
    public BigInteger maxValue() {
        return BigInteger.valueOf(Long.MAX_VALUE);
    }

    @Override
    public BigInteger randomHash() throws HashException {
        return BigInteger.valueOf(random.nextLong());
    }

    public String getHashTreeDOT() {
        return tree.generateDot();
    }
}
