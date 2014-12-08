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

import mendel.tree2.VPPoint;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Add binary prefix to each node in the vantage point tree. Used to as a part
 * of the two-tiered hashing scheme Mendel employs.
 *
 * @author ctolooee
 */
public class VPPrefixTree {

    protected VPPrefixNode root;
    protected  List<VPPoint> points;
    protected double tau;

    public VPPrefixTree(List<VPPoint> points) {
        this.points = points;
        buildTree();
    }

    /**
     * Constructs the vantage-point prefix tree from the list of points stored in
     * <code>points</code>.
     */
    public void buildTree() {
        root = buildFromPoints(0, points.size(), 1);
    }

    private VPPrefixNode buildFromPoints(int lower, int upper, long prefix) {
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
            node.threshold = points.get(lower).getDistanceTo(points.get(median));

            node.index = lower;

            /* Moving down one level adds one bit: left = 0, right = 1 */
            prefix <<= 1;

            node.left = buildFromPoints(lower + 1, median, prefix);

            /* Add 1 for right child */
            node.right = buildFromPoints(median, upper, prefix + 1);
        }
        return node;
    }

    /**
     * Conducts a nearest neighbor search over the vpp-tree with the specified
     * <code>target</code>. The top <code>k</code> results will be stored in
     * <code>results</code> and their distances in <code>distances</code>.
     *
     * @param target    the query value to search for
     * @param k         the number of nearest neighbors
     * @param results   a list the results will be stored in
     * @param distances a list the distances will be stored in
     */
    public void search(VPPoint target, int k, List<VPPoint> results,
                       List<Integer> distances, List<Long> prefixes) {

        PriorityQueue<HeapItem> heap = new PriorityQueue<>();

        /* tau tracks distance of the nearest neighbor that's farthest away */
        tau = Long.MAX_VALUE; // initially infinite

        search(root, target, k, heap);

        /* Iterate through the heap and fill out the results/distances */
        while (!heap.isEmpty()) {
            results.add(points.get(heap.peek().index));
            distances.add(heap.peek().distance);
            prefixes.add(heap.peek().prefix);
            heap.poll();
        }
    }

    protected void search(VPPrefixNode node, VPPoint target, int k,
                          PriorityQueue<HeapItem> heap) {
        /* null node means nothing to search for */
        if (node == null) {
            return;
        }

        /* Get distance from target */
        int dist = (int) points.get(node.index).getDistanceTo(target);

        /* If distance less than tau, its a nearest neighbor found, THUS FAR */
        if (dist < tau) {
            /* Only store top k results */
            if (heap.size() == k) {
                heap.poll();
            }
            heap.add(new HeapItem(node.index, dist, node.prefix));

            /* If the insertion makes the heap full, tau may have changed */
            if (heap.size() == k) {
                tau = heap.peek().distance;
            }
        }

        /* We're at a leaf node; Abandon ship! */
        if (node.left == null && node.right == null) {
            return;
        }

        /* Inside the threshold; search left */
        if (dist < node.threshold) {
            if (dist - tau <= node.threshold) {
                search(node.left, target, k, heap);
            }
            /* Unless there's overlap; search both */
            if (dist + tau >= node.threshold) {
                search(node.right, target, k, heap);
            }
        /* Outside the threshold; search right */
        } else {
            if (dist + tau >= node.threshold) {
                search(node.right, target, k, heap);
            }
            /* Unless there's overlap; search both */
            if (dist - tau <= node.threshold) {
                search((VPPrefixNode) node.left, target, k, heap);
            }
        }
    }


    /**
     * TODO
     * Adds a single node to an existing vp-tree and returns the prefix of the
     * node. If the specified node already exists, it prefix is returned but
     * will not be added a second time.
     *
     * @param newNode node to be added to the vp-prefix-tree
     *
     * @return the prefix of the node in the vpp-tree
     * @throws java.lang.IllegalArgumentException if newNode is null
     */
    public long add(VPPoint newNode) {
        if (newNode == null) {
            throw new IllegalArgumentException();
        }

        return add(root, newNode);
    }

    private long add(VPPrefixNode node, VPPoint newNode) {
         /* null node means something has gone wrong */
        if (node == null) {
            return -1;
        }
        double dist = (int) points.get(node.index).getDistanceTo(newNode);

        /* Check for a match */
        if(dist == 0) {
            return node.prefix;
        }
        /* Find it down the proper subtree */
        else {
            /* Inside the threshold; search left */
            if (dist <= node.threshold) {

                // ---- TODO ------
                /* If child is null, newNode is not in the tree, so add it! */
                /* if(node.left == null) {
                    points.add(newNode);
                    VPPrefixNode n = new VPPrefixNode(points.size() - 1);
                    n.prefix = node.prefix << 1;
                    node.left = n;
                    } else { */
                // ----------------

                add(node.left, newNode);

            /* Outside the threshold; search right */
            } else {
                add(node.right, newNode);

            // ---- TODO ------
                /* If right is null, newNode is not in the tree, so add it! */
                /* if(node.right == null) {
                    points.add(newNode);
                    VPPrefixNode n = new VPPrefixNode(points.size() - 1);
                    n.prefix = node.prefix << 1;
                    node.right = n; */
            // ----------------
            }
        }
        return -1;
    }

    /**
     * Primitive implementation of the C++ std::nth_element algorithm. This
     * method partitions the points list from lower to upper around median such
     * that the values preceding median are less than it and values following
     * are greater.
     */
    protected int nth_element(int lower, int median, int upper, VPPoint vp) {
        int medianDist = (int) points.get(median).getDistanceTo(vp);
        while (lower <= upper) {
            while (points.get(lower).getDistanceTo(vp) < medianDist) {
                lower++;
            }
            while (points.get(upper - 1).getDistanceTo(vp) > medianDist) {
                upper--;
            }
            if (lower <= upper) {
                Collections.swap(points, lower, upper - 1);
                lower++;
                upper--;
            }
        }
        return lower;
    }

    protected class VPPrefixNode {

        private long prefix;
        int index;
        double threshold;
        VPPrefixNode left;
        VPPrefixNode right;

        public VPPrefixNode(int index) {
            this.index = index;
            this.prefix = 0;
        }

        public VPPrefixNode(int index, long prefix) {
            this.index = index;
            this.prefix = prefix;
        }

        public long getPrefix() {
            return prefix;
        }
    }

    protected class HeapItem implements Comparable<HeapItem> {
        int index, distance;
        long prefix;

        public HeapItem(int index, int distance, long prefix) {
            this.index = index;
            this.distance = distance;
            this.prefix = prefix;
        }

        @Override
        public int compareTo(HeapItem o) {
            if (o == null) {
                throw new IllegalArgumentException();
            }
            /* Inverted to ensure polling (popping) from the queue removes the
            value with the largest distance */
            return o.distance - distance;
        }
    }
}
