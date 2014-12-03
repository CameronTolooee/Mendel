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

import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Add binary prefix to each node in the vantage point tree. Used to as a part
 * of the two-tiered hashing scheme Mendel employs.
 */
public class VPPrefixTree extends VPTree {
    private class VPPrefixNode extends VPNode {

        private long prefix;

        public VPPrefixNode(int index) {
            super(index);
            prefix = 0;
        }

        public VPPrefixNode(int index, long prefix) {
            super(index);
            this.prefix = prefix;
        }

        public long getPrefix() {
            return prefix;
        }
    }

    public VPPrefixTree(List<VPPoint> points) {
        super(points);
    }

    @Override
    public void buildTree() {
        root = buildFromPoints(0, points.size(), 1);
    }

    private VPNode buildFromPoints(int lower, int upper, long prefix) {
        if (upper == lower) {
            return null;
        }

        /* Create node with prefix from it's parent */
        VPPrefixNode node = new VPPrefixNode(lower, prefix);

        if (upper - lower > 1) {
            Random rand = new Random();

            /* Choose a random value as the pivot and move it to the front */
            int i = rand.nextInt(upper - 1);
            Collections.swap(points, lower, i);

            /* Partition around the median distance */
            int median = (upper + lower) / 2;
            nth_element(lower + 1, median, upper, points.get(lower));

            /* The node's threshold is the median distance of its partition */
            node.threshold = points.get(lower).distance(points.get(median));

            node.index = lower;
            System.out.println("Node: " + points.get(node.index)
                    + " : " + prefix);

            /* Moving down one level adds one bit: left = 0, right = 1 */
            prefix <<= 1;

            node.left = buildFromPoints(lower + 1, median, prefix);

            /* Add 1 for right child */
            node.right = buildFromPoints(median, upper, prefix + 1);
        }
        return node;
    }

    /**
     * TODO
     * Add a single node to an existing vp-tree
     * @param newNode node to be added to the vp-prefix-tree
     */
    public void add(VPPrefixNode newNode) {

    }
}
