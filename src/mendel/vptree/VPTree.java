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

package mendel.vptree;
import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class VPTree<E extends VPPoint> {

    /**
     * The default node capacity ({@value} points) for nodes in this vptree.
     */
    public static final int DEFAULT_BIN_SIZE = 32;

    private final int binSize;

    private VPNode<E> root;

    /**
     * Constructs a new, empty vp-vptree with a default node capacity.
     */
    public VPTree() {
        this(DEFAULT_BIN_SIZE);
    }

    /**
     * Constructs a new, empty vp-vptree with the specified node capacity.
     *
     * @param nodeCapacity  the maximum number of points to store in a leaf node
     *                        of the vptree
     */
    public VPTree(int nodeCapacity) {
        if(nodeCapacity < 1) {
            throw new IllegalArgumentException("Node capacity must be " +
                    "greater than zero.");
        }

        this.binSize = nodeCapacity;
        this.root = new VPNode<>(binSize, 1);
    }

    /**
     * Constructs a new vp-vptree that contains (and indexes) all of the points in
     * the given collection. Nodes of the vptree are created with a default
     * capacity.
     *
     * @param points
     *            the points to use to populate this vptree
     */
    public VPTree(Collection<E> points) {
        this(points, DEFAULT_BIN_SIZE);
    }

    /**
     * Constructs a new vp-vptree that contains (and indexes) all of the points in
     * the given collection and has leaf nodes with the given point capacity.
     *
     * @param points  the points to use to populate this vptree
     * @param nodeCapacity  the largest number of points any leaf node of the
     *                        vptree should contain
     */
    public VPTree(Collection<E> points, int nodeCapacity) {
        if(nodeCapacity < 1) {
            throw new IllegalArgumentException("Node capacity must be" +
                    " greater than zero.");
        }

        this.binSize = nodeCapacity;

        if(!points.isEmpty()) {
            @SuppressWarnings("unchecked")
            E[] pointArray = points.toArray((E[])Array.newInstance(
                    points.iterator().next().getClass(), 0));

            this.root = new VPNode<>(pointArray, 0, pointArray.length,
                    this.binSize, 1);
        } else {
            this.root = new VPNode<>(this.binSize, 1);
        }
    }

    /**
     * Returns a reference to this vptree's root node. This method is intended for
     * testing purposes only.
     *
     * @return a reference to this vptree's root node
     */
    protected VPNode<E> getRoot() {
        return this.root;
    }

    /**
     * Returns the maximum number of points any leaf node of this vptree should
     * contain.
     *
     * @return the maximum number of points any leaf node should contain
     */
    public int getBinSize() {
        return this.binSize;
    }

    /**
     * Adds a single point to this vp-vptree. Addition of a point executes in
     * O(log n) time in the best case (where n is the number of points in the
     * vptree), but may also trigger a node partition that takes additional time.
     *
     * @param point  the point to add to this vptree
     *
     * @return {@code true} if the vptree was modified by the addition of this
     *         point; vp-trees are always modified by adding points, so this
     *         method always returns true
     */
    public long add(E point) {
        return this.root.add(point);
    }

    /**
     * Adds all of the points in the given collection to this vp-vptree.
     *
     * @param points  the points to add to this vptree
     *
     * @return {@code true} if the vptree was modified by the addition of the
     *         points; vp-trees are always modified by adding points, so this
     *         method always returns true
     */
    public boolean addAll(Collection<? extends E> points) {
        return this.root.addAll(points);
    }

    /**
     * Removes all points from this vp-vptree. Clearing a vp-vptree executes in O(1)
     * time.
     */
    public void clear() {
        this.root = new VPNode<>(this.binSize, 1);
    }

    /**
     * Tests whether this vp-vptree contains the given point. Membership tests
     * execute in O(log n) time, where n is the number of points in the vptree.
     *
     * @param o  the object to test for membership in this vptree
     *
     * @return {@code true} if this vptree contains the given point or
     *         {@code false} otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        try {
            return this.root.contains((E)o);
        } catch(ClassCastException e) {
            return false;
        }
    }

    /**
     * Tests whether this vp-vptree contains all of the points in the given
     * collection. Group membership tests execute in O(m log n) time, where m is
     * the number of points in the given collection and n is the number of
     * points in the vptree.
     *
     * @param c  the collection of points to test for membership in this vptree
     *
     * @return {@code true} if this vptree contains all of the members of the
     *         given collection or {@code false} otherwise
     */
    public boolean containsAll(Collection<?> c) {
        for(Object o : c) {
            if(!this.contains(o)) { return false; }
        }

        return true;
    }

    public long getPrefixOf(Kmer value) {
        if(size() < 1) {
            return -1;
        }
        return root.getPrefixOf(value);
    }

    /**
     * Tests whether this vptree is empty.
     *
     * @return {@code true} if this vptree contains no points or {@code false}
     *         otherwise
     */
    public boolean isEmpty() {
        return this.root.isEmpty();
    }


    /**
     * Removes a point from this vptree.
     *
     * @param o  the point to remove
     *
     * @return {@code true} if the vptree was modified by removing this point
     *         (i.e. if the point was present in the vptree) or {@code false}
     *         otherwise
     */
    public boolean remove(Object o) {
        try {
            @SuppressWarnings("unchecked")
            E point = (E)o;

            return this.remove(point, false, null);
        } catch(ClassCastException e) {
            // The object we were given wasn't the kind of thing we're storing,
            // so we definitely can't remove it.
            return false;
        }
    }

    /**
     * Removes a point from this vptree and optionally defers pruning of nodes
     * left empty after the removal of their last point. If pruning is deferred,
     * it is the responsibility of the caller to prune nodes after this method
     * has executed.
     *
     * @param point  the point to remove
     * @param deferPruning
     *            if {@code true} and the removal of the given point would leave
     *            a node empty, pruning of the empty node is deferred until a
     *            time chosen by the caller; otherwise, empty nodes are pruned
     *            immediately
     * @param nodesToPrune
     *            a {@code Set} to be populated with nodes left empty by the
     *            removal of points; this may be {@code null} if
     *            {@code deferPruning} is {@code false}
     *
     * @return {@code true} if the vptree was modified by removing this point
     *         (i.e. if the point was present in the vptree) or {@code false}
     *         otherwise
     */
    protected boolean remove(E point, boolean deferPruning,
                             Set<VPNode<E>> nodesToPrune) {
        ArrayDeque<VPNode<E>> stack = new ArrayDeque<>();
        this.root.findNodeContainingPoint(point, stack);

        VPNode<E> node = stack.pop();

        boolean pointRemoved = node.remove(point);

        if(node.isEmpty()) {
            if(deferPruning) {
                nodesToPrune.add(node);
            } else {
                this.pruneEmptyNode(node);
            }
        }

        return pointRemoved;
    }

    /**
     * Removes all of the points in the given collection from this vptree.
     *
     * @param c
     *            the collection of points to remove from this true
     *
     * @return {@code true} if the vptree was modified by removing the given
     *         points (i.e. if any of the points were present in the vptree) or
     *         {@code false} otherwise
     */
    public boolean removeAll(Collection<?> c) {
        boolean anyChanged = false;
        HashSet<VPNode<E>> nodesToPrune = new HashSet<>();

        for(Object o : c) {
            try {
                @SuppressWarnings("unchecked")
                E point = (E)o;

                // The behavioral contact for Collections states, "After this
                // call returns, this collection will contain no elements in
                // common with the specified collection." Make sure we remove
                // all instances of each point in the collection of points to
                // remove.
                while(this.remove(point, true, nodesToPrune)) {
                    anyChanged = true;
                }
            } catch(ClassCastException e) {
                /* The object wasn't the kind of point contained in this vptree */
            }
        }

        /* Avoid duplicating work by removing pruning targets that are children
           of other pruning targets (since they would be implicitly pruned by
           pruning the parent). */
        HashSet<VPNode<E>> nodesToNotPrune = new HashSet<>();

        for(VPNode<E> node : nodesToPrune) {
            for(VPNode<E> potentialAncestor : nodesToPrune) {
                if(potentialAncestor.isAncestorOfNode(node)) {
                    nodesToNotPrune.add(node);
                }
            }
        }

        nodesToPrune.removeAll(nodesToNotPrune);

        /* Now the set of nodes to prune contains only the highest nodes in any
           branch; prune (and potentially repartition) each of those
           individually. */
        for(VPNode<E> node : nodesToPrune) {
            this.pruneEmptyNode(node);

            if(node.isOverloaded()) {
                try {
                    node.partition();
                } catch(Exception e) {
                    /* This happens sometimes */
                }
            }
        }

        return anyChanged;
    }

    /**
     * "Prunes" an empty leaf node from the vptree. When a node is pruned, its
     * parent absorbs the points from both of its child nodes (though only one
     * may actually contain points) and discards its child nodes. If the parent
     * node is empty after the absorption of its child nodes, it is also pruned;
     * this process continues until either an ancestor of the original node is
     * non-empty after absorbing its children or until the root of the vptree is
     * reached.
     * <p>
     * The pruning process may leave an ancestor node overly-full, in which
     * case it is the responsibility of the caller to repartition that node.
     *
     * @param node the empty node to prune from the vptree
     */
    protected void pruneEmptyNode(VPNode<E> node) {
        /* Only spend time working on this if the node is actually empty; it's
           harmless to call this method on a non-empty node, though. */
        if (node.isEmpty() && node != this.root) {
            ArrayDeque<VPNode<E>> stack = new ArrayDeque<>();
            this.root.findNodeContainingPoint(node.getCenter(), stack);

            /* Immediately pop the first node off the stack (since it's
               the empty leaf node given as an argument). */
            stack.pop();

            /* Work through the stack until either a non-empty parent or the
               root of the vptree is found */
            while (stack.peek() != null) {
                VPNode<E> parent = stack.pop();
                parent.absorbChildren();

                /* Return when the parent is non-empty */
                if (!parent.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * Returns the total number of points stored in this vp-vptree.
     *
     * @return the number of points stored in this vp-vptree
     */
    public int size() {
        return this.root.size();
    }

    /**
     * Returns an array containing all of the points in this vp-vptree. The order
     * of the points in the array is not defined.
     *
     * @return an array containing all of the points in this vp-vptree
     */
    public Object[] toArray() {
        Object[] array = new Object[this.size()];
        this.root.addPointsToArray(array);

        return array;
    }

    @SuppressWarnings("unchecked")
    /**
     * Returns an array containing all of the points in this vp-vptree; the
     * runtime type of the returned array is that of the specified array. If the
     * collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.
     * <p>
     * If all of the points in this vptree fit in the specified array with room
     * to spare (i.e., the array has more elements than this vp-vptree), the
     * element in the array immediately following the end of the collection is
     * set to {@code null}
     *
     * @param a
     *            the array into which the elements of this vptree are to be
     *            stored, if it is big enough; otherwise, a new array of the
     *            same runtime type is allocated for this purpose
     *
     * @return an array containing all of the points in this vp-vptree
     */
    public <T> T[] toArray(T[] a) {
        int size = this.size();

        if(a.length < this.size()) {
            return (T[]) Arrays.copyOf(this.toArray(), size, a.getClass());
        } else {
            System.arraycopy(this.toArray(), 0, a, 0, size);

            if(a.length > size) { a[size] = null; }

            return a;
        }
    }

    public List<E> getNearestNeighbors(VPPoint queryPoint, int maxResults) {
        BoundedPriorityQueue<E> results = new BoundedPriorityQueue<>(
                queryPoint, maxResults);
        this.root.getNearestNeighbors(queryPoint, results);
        return results.toSortedList(queryPoint);
    }

    public E getNearestNeighbor(VPPoint queryPoint) {
        BoundedPriorityQueue<E> results = new BoundedPriorityQueue<>(
                queryPoint, 1);
        this.root.getNearestNeighbors(queryPoint, results);
        return results.peek();
    }

    public String generateDot() {
        String dot = "graph VPTree {\n";
        dot += root.generateDot(0);
        dot += "}\n";
        return dot;
    }

}
