package mendel.tree;

import java.lang.reflect.Array;
import java.util.*;

/**
 * {@code VPNodes} are the nodes of a vantage point tree. {@code VPNodes}
 * may or may not be leaf nodes; if they are leaf nodes, they have no
 * children (their child node members will be {@code null}) and they will
 * have a non-null {@code elements} member that contains all of the elements
 * stored in the node.
 * <p>
 * Non-leaf nodes will have non-{@code null} children and contain no
 * elements of their own.
 */
public class VPNode<T extends VPPoint> {
    private VPPoint center;
    private double threshold;

    private VPNode<T> closer;
    private VPNode<T> farther;

    private ArrayList<T> elements;
    private final int binSize;

    /**
     * Constructs a new, empty node with the given capacity.
     *
     * @param binSize the largest number of elements this node should hold
     */
    public VPNode(int binSize) {
        this.binSize = binSize;
        this.elements = new ArrayList<>(0);
        this.center = null;
    }

    /**
     * Constructs a new node that contains a subset of the given array of
     * {@code VPPoints}. If the subset of elements is larger than the given bin
     * capacity, child nodes will be created recursively.
     *
     * @param elements  the array of elements from which to build this node
     * @param lower  the starting index (inclusive) of the subset of
     *                   the array from which to build this node
     * @param upper  the end index (exclusive) of the subset of the array from
     *                  which to build this node
     * @param binSize  the largest number of elements this node should hold
     */
    public VPNode(T[] elements, int lower, int upper, int binSize) {
        this.binSize = binSize;

        if(upper - lower <= binSize) {
            /* All done! This is a leaf node. */
            storeElements(elements, lower, upper);
        } else {
            /* Too many elements to store in a single leaf node;
               try to partition the nodes. */
            try {
                partition(elements, lower, upper);
            } catch(PartitionException e) {
                /* All of the elements we were given are coincident */
                storeElements(elements, lower, upper);
            }
        }
    }

    /**
     * Returns a reference to this node's child that contains elements that
     * are closer to this node's center than this node's distance threshold.
     *
     * @return a reference to this node's "closer" (left) child, or {@code null}
     *         if this is a leaf node
     *
     */
    public VPNode<T> getCloserNode() {
        return closer;
    }

    /**
     * Returns a reference to this node's child that contains elements that
     * are farther away from this node's center than this node's distance
     * threshold.
     *
     * @return a reference to this node's "farther" (right) child, or
     *         {@code null} if this is a leaf node
     *
     */
    public VPNode<T> getFartherNode() {
        return farther;
    }

    /**
     * Returns a new point that is coincident with this node's center point.
     *
     * @return a new point that is coincident with this node's center point
     */
    public VPPoint getCenter() {
        return new Kmer(center);
    }

    /**
     * Returns the distance threshold for this node if it is a non-leaf
     * node.
     *
     * @return the distance threshold for this node
     *
     * @throws IllegalStateException if this node is a leaf node
     *
     */
    public double getThreshold() {
        if(isLeafNode()) {
            throw new IllegalStateException("Leaf nodes do not have a" +
                    " distance threshold.");
        }

        return threshold;
    }

    /**
     * Adds all of the elements in a collection to this node (if it is a
     * leaf node) or its children. If this node is a leaf node and the added
     * elements push this node beyond its capacity, it is partitioned as
     * needed after all elements have been added.
     *
     * @param elements  the collection of elements to add to this node or its
     *            children
     *
     * @return {@code true} if this node or its children were modified or
     *         {@code false} otherwise
     */
    public boolean addAll(Collection<? extends T> elements) {
        HashSet<VPNode<T>> nodesAffected = new HashSet<>();

        for(T point : elements) {
            add(point, true, nodesAffected);
        }

        /* Fix any affected nodes if need be */
        for(VPNode<T> node : nodesAffected) {
            if(node.isOverloaded()) {
                try {
                    node.partition();
                } catch (PartitionException e) {
                    /* Nothing to do here; */
                }
            } else {
                /* Node isn't full but may need to trim it to size */
                node.elements.trimToSize();
            }
        }

        return !elements.isEmpty();
    }

    /**
     * Adds a point to this node if it is a leaf node or one of its children
     * if not. If the node that ultimately holds the new point is loaded
     * beyond its capacity, it will be partitioned.
     *
     * @param point  the point to add to this node or one of its children
     *
     * @return {@code true} if this node or one of its children was modified
     *         by the addition of this point or {@code false} otherwise
     */
    public boolean add(T point) {
        return add(point, false, null);
    }

    /**
     * Adds a point to this node if it is a leaf node or one of its
     * children if not. If the node that ultimately holds the new point is
     * loaded beyond its capacity, it will be partitioned.
     * <p>
     * Partitioning may optionally be deferred, in which case it is the
     * responsibility of the caller to partition overloaded nodes.
     *
     * @param point
     *            the point to add to this node or one of its children
     * @param deferMaintenance
     *            if {@code true}, defer partitioning of overloaded nodes
     *            and trimming of nodes with spare capacity until the caller
     *            chooses to partition or trim them; if {@code false},
     *            overloaded nodes are partitioned or trimmed immediately
     * @param nodesAffected
     *            a {@code Set} that collects nodes that have received new
     *            elements; this may be {@code null} if
     *            {@code deferMaintenance} is {@code false}. Callers must
     *            use this set to partition or trim nodes later.
     *
     * @return {@code true} if this node or any of its children were
     *         modified by the addition of the new point or {@code false}
     *         otherwise; note that adding elements always results in
     *         modification
     */
    protected boolean add(T point, boolean deferMaintenance,
                          Set<VPNode<T>> nodesAffected) {
        if(isLeafNode()) {
            elements.add(point);

            if(deferMaintenance) {
                nodesAffected.add(this);
            } else {
                /* Maintenance may have to be done */
                if(isOverloaded()) {
                    try {
                        partition();
                    } catch(PartitionException e) {
                        /* Nothing to do here; just hold on to all of our */
                    }
                } else {
                /* Node isn't full but may need to trim it to size */
                    elements.trimToSize();
                }
            }
        } else {
            if(center.getDistanceTo(point) <= threshold) {
                return closer.add(point);
            } else {
                return farther.add(point);
            }
        }

        return true;
    }

    /**
     * Tests whether this node or one of its children contains the given
     * point.
     *
     * @param point
     *            the point whose presence is to be tested
     *
     * @return {@code true} if the given point is present in this node or
     *         one of its children or {@code false} otherwise
     */
    public boolean contains(T point) {
        if(isLeafNode()) {
            return elements.contains(point);
        } else {
            if(center.getDistanceTo(point) <= threshold) {
                return closer.contains(point);
            } else {
                return farther.contains(point);
            }
        }
    }

    /**
     * Returns the number of elements contained in this node and its child
     * nodes.
     *
     * @return the number of elements in this node and its children
     */
    public int size() {
        if(isLeafNode()) {
            return elements.size();
        } else {
            return closer.size() + farther.size();
        }
    }

    /**
     * Stores a subset of an array of elements in this node directly, making
     * this node a leaf node.
     *
     * @param elements  the array of elements from which to store a subset
     * @param lower  the starting index (inclusive) of the subset of the array
     *                 to store
     * @param upper  the end index (exclusive) of the subset of the array to
     *                 store
     */
    private void storeElements(T[] elements, int lower, int upper) {
        this.elements = new ArrayList<>(upper - lower);

        for(int i = lower; i < upper; i++) {
            this.elements.add(elements[i]);
        }

        /* Always choose a center point if there isn't one already */
        if(this.center == null && !this.elements.isEmpty()) {
            this.center = new Kmer(this.elements.get(0));
        }

        this.closer = null;
        this.farther = null;
    }

    /**
     * Returns a collection of all the elements stored directly in this node.
     *
     * @return a collection of all the elements stored directly in this node
     *
     * @throws IllegalStateException if this node is not a leaf node
     */
    public Collection<T> getElements() {
        if(!isLeafNode()) {
            throw new IllegalStateException("Cannot retrieve elements from a" +
                    " non-leaf node.");
        }
        return new ArrayList<>(elements);
    }

    /**
     * Attempts to partition the elements contained in this node into two
     * child nodes. Partitioning this node may trigger recursive
     * partitioning attempts in the generated child nodes.
     *
     * @throws PartitionException
     *             if this node is node a leaf node, if this node is empty,
     *             or if no viable distance threshold could be found
     */
    public void partition() throws PartitionException {
        if(!isLeafNode()) {
            throw new PartitionException("Cannot partition a non-leaf node.");
        }

        if(!isEmpty()) {
            @SuppressWarnings("unchecked")
            T[] pointArray = elements.toArray((T[]) Array.newInstance(
                    elements.iterator().next().getClass(), 0));

            partition(pointArray, 0, pointArray.length);
        } else {
            throw new PartitionException("Cannot partition an empty node.");
        }
    }

    /**
     * Attempts to partition the elements in a subset of the given array into
     * two child nodes based on their distance from the center of this node.
     * This method chooses a center point if none exists and chooses a
     * distance threshold to use as the criterion for node partitioning. The
     * threshold is chosen to be as close to the median distance of the
     * elements in the sub-array as possible while still partitioning the
     * elements into two groups. The child nodes generated by this method may
     * be partitioned recursively.
     *
     * @param elements  an array from which to partition a subset of elements
     * @param lower  the start index of the sub-array of elements to
     *                     partition (inclusive)
     * @param upper  the end index of the sub-array of elements to partition
     *                   (exclusive)
     *
     * @throws PartitionException
     *             if the range specified by {@code lower} and
     *             {@code upper} includes fewer than two elements or if no
     *             viable distance threshold could be found (i.e. all of the
     *             elements in the subarray have the same distance from this
     *             node's center point)
     */
    protected void partition(T[] elements, int lower,
                             int upper) throws PartitionException {

        if(upper - lower < 2) {
            throw new PartitionException("Cannot partition fewer" +
                    " than two elements.");
        }

        /* Choose a center point and distance threshold (the median distance) */
        if(center == null) {
            center = new Kmer(elements[lower]);
        }

        /* Find the median element with selection algorithm at the middle pos */
        int median = (lower + upper - 1) / 2;
        nth_element(elements, lower, upper-1, median, center);
        //Arrays.sort(elements, lower, upper);

        double medianDistance = center.getDistanceTo(elements[median]);

        /* Left subtree will contain elements <= the threshold. Make sure there
           is at least one point further away than the threshold or this node
           will have no right children. */
        int partitionIndex = -1;

        for(int i = median + 1; i < upper; i++) {
            if(center.getDistanceTo(elements[i]) > medianDistance) {
                partitionIndex = i;
                threshold = center.getDistanceTo(elements[i]);
                /* Found one! Break free! */
                break;
            }
        }

        /* If no such point exists, lower the threshold until a point whose
           distance is greater than the threshold found. If the median distance
           is 0 then there is no such element. */
        if(partitionIndex == -1) {
            if(medianDistance != 0) {
                for(int i = median; i > lower; i--) {
                    if(center.getDistanceTo(elements[i]) < medianDistance) {
                        partitionIndex = i;
                        threshold = center.getDistanceTo(elements[i]);

                        break;
                    }
                }

                /* Everything except the center has the same non-zero distance
                  from the center, therefore partition the center by itself
                  on the left (closer) and everything else on the right
                  (further) */
                if(partitionIndex == -1) {
                    partitionIndex = lower + 1;
                }
            }
        }

        /* All elements have equals distance, no partitioning is possible */
        if(partitionIndex == -1) {
            throw new PartitionException(
                    "No viable partition threshold found (all elements have" +
                            " equal distance from center).");
        }

        /* Partition the array */
        closer = new VPNode<>(elements, lower,
                partitionIndex, binSize);
        farther = new VPNode<>(elements, partitionIndex,
                upper, binSize);

        /* No longer a leaf nodes */
        this.elements = null;
    }

    /**
     * Tests whether this is a leaf node.
     *
     * @return {@code true} if this node is a leaf node or {@code false}
     *         otherwise
     */
    public boolean isLeafNode() {
        return closer == null;
    }

    /**
     * Tests whether this node and all of its children are empty.
     *
     * @return {@code true} if this node and all of its children contain no
     *         elements or {@code false} otherwise
     */
    public boolean isEmpty() {
        if(isLeafNode()) {
            return elements.isEmpty();
        } else {
            return (closer.isEmpty() && farther.isEmpty());
        }
    }

    /**
     * Tests whether this node contains more elements than its maximum
     * capacity.
     *
     * @return {@code true} if the number of elements stored in this node is
     *         greater than its capacity or {@code false} otherwise
     *
     * @throws IllegalStateException if this is not a leaf node
     */
    public boolean isOverloaded() {
        if(!isLeafNode()) {
            throw new IllegalStateException("Non-leaf nodes" +
                    " cannot be overloaded.");
        }
        return elements.size() > binSize;
    }

    /**
     * Populates the given search result set with elements close to the query
     * point. If this node is a leaf node, all of its contained elements are
     * "offered" to the search result set as potential nearest neighbors. If
     * this is not a leaf node, one or both of its children are searched
     * recursively.
     *
     * @param queryPoint  the point for which to find nearby neighbors
     * @param results  the result set to which to offer elements
     */
    public void getNearestNeighbors(final VPPoint queryPoint,
                                    BoundedPriorityQueue<T> results) {

        /* If this is a leaf node, offer all elements */
        if(isLeafNode()) {
            results.addAll(elements);
        } else {
            /* Descend through the tree recursively */
            boolean searchedCloserFirst;
            double distanceToCenter = center.getDistanceTo(queryPoint);

            if(distanceToCenter <= threshold) {
                closer.getNearestNeighbors(queryPoint, results);
                searchedCloserFirst = true;
            } else {
                farther.getNearestNeighbors(queryPoint, results);
                searchedCloserFirst = false;
            }

            /* Decide to search whichever child  wasn't searched on the
              way down */
            if(searchedCloserFirst) {
                double distanceToThreshold = threshold - distanceToCenter;

                if(results.getFurthestDistance() > distanceToThreshold) {
                    farther.getNearestNeighbors(queryPoint, results);
                }
            } else {
                double distanceToThreshold = distanceToCenter - threshold;

                if(distanceToThreshold <= results.getFurthestDistance()) {
                    closer.getNearestNeighbors(queryPoint, results);
                }
            }
        }
    }

    /**
     * Adds all of the elements from this node if it is a leaf node or its
     * children if it is not to an array. It is the responsibility of the
     * caller to ensure that the array has sufficient capacity to hold all
     * of the elements in this node and its children.
     *
     * @param array  the array to which to add elements
     *
     */
    public void addPointsToArray(Object[] array) {
        addPointsToArray(array, 0);
    }

    /**
     * Adds all of the elements from this node and its children to the given
     * array starting at the given offset. It is the responsibility of the
     * caller to ensure that the array has sufficient capacity to hold all
     * of the elements in this node and its children.
     *
     * @param array  the array to which to ad elements
     * @param offset  the starting index (inclusive) of the array to begin
     *                  adding elements
     *
     * @return the number of elements added to the array
     */
    public int addPointsToArray(Object[] array, int offset) {
        if(isLeafNode()) {
            if(isEmpty()) {
                return 0;
            }

            System.arraycopy(elements.toArray(), 0, array,
                    offset, elements.size());

            return elements.size();
        } else {
            int nAddedFromCloser = closer.addPointsToArray(array, offset);
            int nAddedFromFarther = farther.addPointsToArray(array,
                    offset + nAddedFromCloser);

            return nAddedFromCloser + nAddedFromFarther;
        }
    }

    /**
     * Finds the node at or below this node that contains (or would contain)
     * the given point. The given stack is populated with each node on the
     * path to the leaf node that contains the given point.
     *
     * @param p  the point for which to find the containing node
     * @param stack  the stack to populate with the chain of nodes leading to
     *                 the leaf node that contains or would contain the given
     *                 point
     */
    public void findNodeContainingPoint(final VPPoint p,
                                        final Deque<VPNode<T>> stack) {
        // First things first; add ourselves to the stack.
        stack.push(this);

        // If this is a leaf node, we don't need to do anything else. If
        // it's not a leaf node, recurse!
        if(!isLeafNode()) {
            if(center.getDistanceTo(p) <= threshold) {
                closer.findNodeContainingPoint(p, stack);
            } else {
                farther.findNodeContainingPoint(p, stack);
            }
        }
    }

    /**
     * Removes a point from this node's internal list of elements.
     *
     * @param point  the point to remove from this node
     *
     * @return {@code true} if the point was removed from this node (i.e.
     *         this node actually contained the given point) or
     *         {@code false} otherwise
     *
     * @throws IllegalStateException  if this node is not a leaf node
     */
    public boolean remove(T point) {
        if(isLeafNode()) {
            boolean pointRemoved = elements.remove(point);

            if(pointRemoved) {
                elements.trimToSize();
            }

            return pointRemoved;
        } else {
            throw new IllegalStateException("Cannot remove elements from" +
                    " a non-leaf node.");
        }
    }

    /**
     * Recursively absorbs the elements contained in this node's children into
     * this node, making this node a leaf node in the process.
     *
     * @throws IllegalStateException if this node is a leaf node
     */
    public void absorbChildren() {
        if(isLeafNode()) {
            throw new IllegalStateException("Leaf nodes have no children " +
                    "to absorb.");
        }

        elements = new ArrayList<>(size());

        if(!closer.isLeafNode()) {
            closer.absorbChildren();
        }

        if(!farther.isLeafNode()) {
            farther.absorbChildren();
        }

        elements.addAll(closer.getElements());
        elements.addAll(farther.getElements());

        closer = null;
        farther = null;
    }

    /**
     * Populates the given {@code List} with all of the leaf nodes that are
     * descendants of this node.
     *
     * @param leafNodes  the list to populate with leaf nodes
     */
    public void gatherLeafNodes(List<VPNode<T>> leafNodes) {
        if(isLeafNode()) {
            leafNodes.add(this);
        } else {
            closer.gatherLeafNodes(leafNodes);
            farther.gatherLeafNodes(leafNodes);
        }
    }

    /**
     * Tests whether this node is an ancestor of the given node.
     *
     * @param node  the node for which to test ancestry
     *
     * @return {@code true} if the given node is a descendant of this node
     *         or {@code false} otherwise
     */
    public boolean isAncestorOfNode(VPNode<T> node) {
        /* Leaf nodes can't be the ancestors of anything */
        if(isLeafNode()) {
            return false;
        }

        /* Find a path to the center of the given node */
        ArrayDeque<VPNode<T>> pathToRoot = new ArrayDeque<>();
        findNodeContainingPoint(node.getCenter(), pathToRoot);

        return pathToRoot.contains(this);
    }


    /**
     * Implementation of the selection algorithm. This
     * method partitions the {@code elements} array from {@code lower} to
     * {@code upper} around n such that the values preceding n are less than it
     * and values following are greater. This is a more efficient way to find
     * the median of a collection than sorting and selecting the middle element.
     * The runtime is O(n) in all cases. This algorithm is run
     * <strong>in place</strong> and therefore will modify the elements array.
     * 
     * @param elements  the array to perform the selection algorithm on
     * @param lower  the lower bound (inclusive) of the range to be partitioned
     * @param upper  the upper bound (inclusive) of the range to be partitioned
     * @param n  the position to partition around
     * @return the index of the n'th element
     */
    public int nth_element(T[] elements, int lower,
                              int upper, int n, VPPoint vp) {

        if (elements == null || upper <= n || lower >= n) {
            throw new IllegalArgumentException();
        }
        int medianDist = (int) elements[n].getDistanceTo(vp);
        while (lower <= upper) {
            while (elements[lower].getDistanceTo(vp) < medianDist) {
                lower++;
            }
            while (elements[upper - 1].getDistanceTo(vp) > medianDist) {
                upper--;
            }
            if (lower <= upper) {
                T temp = elements[lower];
                elements[lower] = elements[upper];
                elements[upper] = temp;

                lower++;
                upper--;
            }
        }
        return n;

//        /* If from == to we reached the kth element */
//        while (from < to) {
//            int r = from, w = to;
//            //T mid = elements[(r + w) / 2];
//
//            /* Stop if r and w meet */
//            while (r < w) {
//                /* put the large values at the end */
//                if (elements[r].compareTo(vp) > 0) {
//                    T tmp = elements[w];
//                    elements[w] = elements[r];
//                    elements[r] = tmp;
//                    w--;
//                } else { /* the value is smaller than the pivot, skip */
//                    r++;
//                }
//            }
//            /* If we stepped up (r++) we need to step one down */
//            if (elements[r].compareTo(vp) > 0) {
//                r--;
//            }
//            /* The r pointer is on the end of the first k elements */
//            if (n <= r) {
//                to = r;
//            } else {
//                from = r + 1;
//            }
//        }
//        return n;
    }
}