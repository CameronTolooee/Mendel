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

import mendel.vptree.Kmer;
import mendel.vptree.VPTree;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testing correctness of vp-vptree implementation.
 *
 * @author ctolooee
 */
public class VPTreeTest {

    List<Kmer> list;

    @Before
    public void setup() {

        /* Create list of k-mers */
        list = new ArrayList<>();
        list.add(new Kmer("ACTGCCTGA"));
        list.add(new Kmer("ACTTCCTGA"));
        list.add(new Kmer("ACTCCCTGA"));
        list.add(new Kmer("AAGGCCTGA"));
        list.add(new Kmer("GGTGCCTGA"));
        list.add(new Kmer("CGTGATGCA"));
        list.add(new Kmer("ACCCCCCCC"));
        list.add(new Kmer("AAAAAAACC"));
        list.add(new Kmer("AAAAAACCC"));
        list.add(new Kmer("AAAAACCCC"));
        list.add(new Kmer("AAAACCCCC"));
        list.add(new Kmer("AAACCCCCC"));
        list.add(new Kmer("ACCCCCCCC"));
    }


    @Test
    public void testVPTree() {

        VPTree<Kmer> vpTree = new VPTree<>(list, 3);

        Kmer target = new Kmer("ACCCCCCCT"); /* Matches ACCCCCCCC" */

        Kmer nearestNeighbor = vpTree.getNearestNeighbor(target);

        System.out.println(nearestNeighbor);
        assertEquals(nearestNeighbor, new Kmer("ACCCCCCCC"));
        //testDOT();
    }

    private void testDOT() {

        VPTree<Kmer> vpTree = new VPTree<>(list, 3);
        VPTree<Kmer> vpTree2 = new VPTree<>(3);
        for (Kmer kmer : list) {
            vpTree2.add(kmer);
        }

        Kmer[] array = new Kmer[vpTree2.size()];
        vpTree2.toArray(array);
        List<Kmer> list2 = Arrays.asList(array);
        VPTree<Kmer> vpTree3 = new VPTree<>(list2, 3);

        System.out.println(vpTree.generateDot());
        System.out.println(vpTree2.generateDot());
        System.out.println(vpTree3.generateDot());
    }
}
