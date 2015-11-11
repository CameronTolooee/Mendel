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

package mendel.test;

import mendel.data.parse.FastaParser;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.Serializer;
import mendel.vptree.types.ProteinSequence;
import mendel.vptree.types.Sequence;
import mendel.vptree.VPTree;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testing correctness of vp-tree implementation.
 *
 * @author ctolooee
 */
public class VPTreeTest {

    List<Sequence> list;

    @Before
    public void setup() {

        /* Create list of k-mers */
        list = new ArrayList<>();
        list.add(new Sequence("ACTGCCTGA"));
        list.add(new Sequence("ACTTCCTGA"));
        list.add(new Sequence("ACTCCCTGA"));
        list.add(new Sequence("AAGGCCTGA"));
        list.add(new Sequence("GGTGCCTGA"));
        list.add(new Sequence("CGTGATGCA"));
        list.add(new Sequence("ACCCCCCCC"));
        list.add(new Sequence("AAAAAAACC"));
        list.add(new Sequence("AAAAAACCC"));
        list.add(new Sequence("AAAAACCCC"));
        list.add(new Sequence("AAAACCCCC"));
        list.add(new Sequence("AAACCCCCC"));
        list.add(new Sequence("ACCCCCCCC"));
    }


    @Test
    public void testVPTree() throws IOException {

        VPTree<ProteinSequence> vpTree = new VPTree<>(list, 3);
        FastaParser parser = new FastaParser("data/staph-query-lines");
        Iterator<ProteinSequence> windowIterator = parser.windowIterator();
        while(windowIterator.hasNext()) {
            vpTree.add(windowIterator.next());
        }
        FileOutputStream fOut = new FileOutputStream(new File("test.index"));
        fOut.write(Serializer.serialize(vpTree));
        FileInputStream fIn = new FileInputStream(new File("test.index"));
        SerializationInputStream sIn = new SerializationInputStream(fIn);
        VPTree<Sequence> vpTree2 = new VPTree<>(sIn);
        System.out.println(vpTree2.size());
    }

    private void testDOT() {

        VPTree<Sequence> vpTree = new VPTree<>(list, 3);
        VPTree<Sequence> vpTree2 = new VPTree<>(3);
        for (Sequence sequence : list) {
            vpTree2.add(sequence);
        }

        Sequence[] array = new Sequence[vpTree2.size()];
        vpTree2.toArray(array);
        List<Sequence> list2 = Arrays.asList(array);
        VPTree<Sequence> vpTree3 = new VPTree<>(list2, 3);

        System.out.println(vpTree.generateDot());
        System.out.println(vpTree2.generateDot());
        System.out.println(vpTree3.generateDot());
    }
}
