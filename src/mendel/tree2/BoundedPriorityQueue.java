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

package mendel.tree2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @param <E>
 * @author ctolooee
 */
public class BoundedPriorityQueue<E extends VPPoint> extends PriorityQueue<E> {

    private VPPoint center;
    private int maxSize;
    private double furthestDistance;

    /**
     * @param center  the query point to measure versus; i.e. the center of the
     *                initial query
     * @param maxSize the maximum capacity (upper bound) on the number of
     *                elements that can be stored in the queue
     */
    public BoundedPriorityQueue(VPPoint center, int maxSize) {
        this.center = center;
        this.maxSize = maxSize;
    }

    /**
     * Attempts to add an element to the bounded queue. If the queue is full and
     * the element to add is further away from the vantage point, the element
     * will be discarded.
     *
     * @param element the element to be added to the queue
     * @return true if the element was added; false otherwise
     */
    public boolean add(E element) {
        boolean added = false;
        if (size() >= maxSize) {
            if (this.getFurthestDistance() >= element.getDistanceTo(center)) {
                this.poll();
                added = super.add(element);
            }
        } else {
            added = super.add(element);
        }
        return added;
    }

    /**
     * Attempts to add {@link java.util.Collection} of elements to the bounded
     * queue. If the queue is full and the element(s) to add are further away
     * from the vantage point, the element(s) will be discarded.
     *
     * @param collection the collection of elements to attempt to add to the
     *                   queue
     * @return true if an element is added; false otherwise
     */
    public boolean addAll(Collection<? extends E> collection) {
        if (this.equals(collection)) {
            throw new IllegalArgumentException("Cannot addAll of a queue to itself.");
        }

        boolean added = false;
        for (E e : collection) {
            if (this.add(e)) {
                added = true;
            }
        }
        return added;
    }

    /**
     * Finds the distance of the element who is furthest from the center.
     * Essentially this is the distance from the head of the queue to the
     * center. This is functionally equivalent to
     * <code>center.getDistanceTo(peek())</code>
     *
     * @return the distance of the furthest element from the center in the
     *      queue or <code>Double.POSITIVE_INFINITY</code> if the queue is
     *      empty.
     */
    public double getFurthestDistance() {
        double furthest;
        if (super.isEmpty()) {
            furthest = Double.POSITIVE_INFINITY;
        } else {
            furthest = this.center.getDistanceTo(this.peek());
        }
        return furthest;
    }

    /**
     * Returns a list of the points in this result set sorted in order of
     * increasing distance from the query point provided at construction time.
     * The result set itself is not modified by calls to this method.
     *
     * @return a sorted list of the points in this result set
     */
    public List<E> toSortedList() {
        ArrayList<E> sortedList = new ArrayList<>(this);
        java.util.Collections.sort(sortedList);
        return sortedList;
    }
}
