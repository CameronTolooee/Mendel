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

import mendel.util.Pair;
import mendel.tree.Kmer;
import mendel.tree.VPPoint;
import mendel.tree.VPTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Testing correctness of vp-tree implementation.
 *
 * @author ctolooee
 */
public class VPTreeTest {
    public static void main(String[] args) {
        
        /* Create list of k-mers */
        List<VPPoint> list = new ArrayList<>();
        list.add(new Kmer("ACTGCCTGA"));
        list.add(new Kmer("ACTTCCTGA"));
        list.add(new Kmer("ACTCCCTGA"));
        list.add(new Kmer("AAGGCCTGA"));
        list.add(new Kmer("ACTGCCTGA"));
        list.add(new Kmer("GGTGCCTGA"));
        list.add(new Kmer("CGTGATGCA"));
        list.add(new Kmer("ACCCCCCCC"));
        list.add(new Kmer("NNNNNNNNN"));

        VPTree vpTree = new VPTree(list);
        vpTree.buildTree();

        Kmer target = new Kmer("GGTGCCTGG"); /* Matches GGTGNCTGA" */
        List<VPPoint> values = new ArrayList<>();
        List<Integer> distances = new ArrayList<>();
        System.out.println("Searching 'GGTGNCTGG' (matches 'GGTGNCTGA')");
        vpTree.search(target, 5, values, distances);

        for (int i = values.size() - 1; i >= 0; --i) {
            System.out.print(values.get(i));
            System.out.println(" : " + distances.get(i));
        }

        /* Linear search results */
        List<Pair<Kmer, Integer>> linear = new ArrayList<>();
        for (VPPoint kmer : list) {
            int dist = target.distance(kmer);
            linear.add(new Pair<Kmer, Integer>((Kmer) kmer, dist));
        }

        /* Sort results */
        for (int i = 0; i < linear.size(); i++) {
            for (int j = 0; j < linear.size() - 1; j++) {
                if (linear.get(j).b > linear.get(j + 1).b) {
                    Collections.swap(linear, j, j + 1);
                }
            }
        }
        System.out.println("Linear results");
        for (Pair<Kmer, Integer> kmerIntegerPair : linear) {
            System.out.println(kmerIntegerPair);
        }
    }
}
