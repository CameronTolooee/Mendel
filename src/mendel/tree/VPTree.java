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
 * Implementation of the vantage-point tree.
 *
 * @author ctolooee
 */
public final class VPTree<T extends VPPoint<T>> {
    private VPNode root;
    private final List<T> points;
    private double tau;

    public VPTree(List<T> points) {
        this.points = points;
    }

    public void buildTree() {
        root = buildFromPoints(0, points.size());
    }

    public void search(T target, int k, List<T> results, List<Integer> distances) {
        PriorityQueue<HeapItem> heap = new PriorityQueue<>();

        long tau = Long.MAX_VALUE;

        search(root, target, k, heap);

        results.clear();
        distances.clear();

        while (!heap.isEmpty()) {
            results.add(points.get(heap.peek().index));
            distances.add(heap.poll().distance);
            heap.poll();
        }
    }

    private VPNode buildFromPoints(int lower, int upper) {
        VPNode node = new VPNode(lower);

        if (upper - lower > 1) {
            Random rand = new Random();

            // choose an arbitrary point and move it to the start
            int i = rand.nextInt(upper);
            Collections.swap(points, lower, i);

            int median = (upper + lower) / 2;

            // partition around the median distance
            nth_element(lower + 1, median, upper, points.get(lower));

            // what was the median?
            node.threshold = points.get(lower).distance(points.get(median));

            node.index = lower;
            node.left = buildFromPoints(lower + 1, median);
            node.right = buildFromPoints(median, upper);
        }
        return node;
    }

    private void search(VPNode node, T target, int k,
                        PriorityQueue<HeapItem> heap) {
        if (node == null) {
            return;
        }
        int dist = points.get(node.index).distance(target);

        if (dist < tau) {
            if (heap.size() == k) {
                heap.poll();
            }
            heap.add(new HeapItem(node.index, dist));
            if (heap.size() == k) {
                tau = heap.peek().distance;
            }
        }

        if (node.left == null && node.right == null) {
            return;
        }
        if (dist < node.threshold) {
            if (dist - tau <= node.threshold) {
                search(node.left, target, k, heap);
            }
            if (dist + tau >= node.threshold) {
                search(node.right, target, k, heap);
            }
        } else {
            if (dist + tau >= node.threshold) {
                search(node.right, target, k, heap);
            }

            if (dist - tau <= node.threshold) {
                search(node.left, target, k, heap);
            }
        }
    }

    private int nth_element(int lower, int median, int upper, VPPoint<T> vp) {
        int medianDist = points.get(median).distance(vp);
        while (lower <= upper) {
            while (points.get(lower).distance(vp) < medianDist)
                lower++;
            while (points.get(lower).distance(vp) > medianDist)
                upper--;
            if (lower <= upper) {
                Collections.swap(points, lower, upper);
                lower++;
                upper--;
            }
        }
        return lower;
    }

    private class HeapItem implements Comparable<HeapItem> {
        int index, distance;

        public HeapItem(int index, int distance) {
            this.index = index;
            this.distance = distance;
        }

        @Override
        public int compareTo(HeapItem o) {
            return distance - o.distance;
        }
    }
}
