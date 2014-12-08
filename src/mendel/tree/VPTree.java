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
 * Implementation of the vantage-point tree.
 *
 * @author ctolooee
 */
public class VPTree {
    protected VPNode root;
    protected final List<VPPoint> points;
    protected double tau;

    public VPTree(List<VPPoint> points) {
        this.points = points;
    }

    public void buildTree() {
        root = buildFromPoints(0, points.size());
    }

    protected VPNode buildFromPoints(int lower, int upper) {
        if (upper == lower) {
            return null;
        }

        VPNode node = new VPNode(lower);

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
            node.left = buildFromPoints(lower + 1, median);
            node.right = buildFromPoints(median, upper);
        }
        return node;
    }

    /**
     * Conducts a nearest neighbor search over the vp-tree with the specified
     * <code>target</code>. The top <code>k</code> results will be stored in
     * <code>results</code> and their distances in <code>distances</code>.
     *
     * @param target    the query value to search for
     * @param k         the number of nearest neighbors
     * @param results   a list the results will be stored in
     * @param distances a list the distances will be stored in
     */
    public void search(VPPoint target, int k, List<VPPoint> results,
                       List<Integer> distances) {

        PriorityQueue<HeapItem> heap = new PriorityQueue<>();

        /* tau tracks distance of the nearest neighbor that's farthest away */
        tau = Long.MAX_VALUE; // initially infinite

        search(root, target, k, heap);

        /* Iterate through the heap and fill out the results/distances */
        while (!heap.isEmpty()) {
            results.add(points.get(heap.peek().index));
            distances.add(heap.peek().distance);
            heap.poll();
        }
    }

    protected void search(VPNode node, VPPoint target, int k,
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
            heap.add(new HeapItem(node.index, dist));

            /* If the insertion makes the heap full, tau may have changed */
            if (heap.size() == k) {
                tau = heap.peek().distance;
            }
        }

        /* We're at a leaf node; Abandon ship! */
        if (node.left == null && node.right == null) {
            return;
        }

        /* Inside the tau; search left */
        if (dist < node.threshold) {
            if (dist - tau <= node.threshold) {
                search(node.left, target, k, heap);
            }
            /* Unless there's overlap; search both */
            if (dist + tau >= node.threshold) {
                search(node.right, target, k, heap);
            }
        /* Outside the tau; search right */
        } else {
            if (dist + tau >= node.threshold) {
                search(node.right, target, k, heap);
            }
            /* Unless there's overlap; search both */
            if (dist - tau <= node.threshold) {
                search(node.left, target, k, heap);
            }
        }
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

    /**
     * Quick aggregation class to be used in the PriorityQueue.
     */
    protected class HeapItem implements Comparable<HeapItem> {
        int index, distance;

        public HeapItem(int index, int distance) {
            this.index = index;
            this.distance = distance;
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
