package mendel.tree2;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>{@code VPNodes} are the nodes of a vantage point tree. {@code VPNodes}
 * may or may not be leaf nodes; if they are leaf nodes, they have no
 * children (their child node members will be {@code null}) and they will
 * have a non-null {@code points} member that contains all of the points
 * stored in the node.</p>
 *
 * <p>Non-leaf nodes will have non-{@code null} children and contain no
 * points of their own.</p>
 */
public class VPNode<T extends VPPoint> {
    private VPPoint center;
    private double threshold;

    private VPNode<T> closer;
    private VPNode<T> farther;

    private ArrayList<T> points;
    private final int binSize;

    /**
     * Constructs a new, empty node with the given capacity.
     *
     * @param binSize the largest number of points this node should hold
     */
    public VPNode(int binSize) {
        this.binSize = binSize;
        this.points = new ArrayList<T>(0);

        this.center = null;
    }

    /**
     * Constructs a new node that contains a subset of the given array of
     * points. If the subset of points is larger than the given bin
     * capacity, child nodes will be created recursively.
     *
     * @param points
     *            the array of points from which to build this node
     * @param fromIndex
     *            the starting index (inclusive) of the subset of the array
     *            from which to build this node
     * @param toIndex
     *            the end index (exclusive) of the subset of the array from
     *            which to build this node
     * @param binSize
     *            the largest number of points this node should hold
     */
    public VPNode(T[] points, int fromIndex, int toIndex, int binSize) {
        this.binSize = binSize;

        if(toIndex - fromIndex <= binSize) {
            // All done! This is a leaf node.
            this.storePoints(points, fromIndex, toIndex);
        } else {
            // We have more points than we want to store in a single leaf
            // node; try to partition the nodes.
            try {
                this.partition(points, fromIndex, toIndex);
            } catch(PartitionException e) {
                // Partitioning failed; this is most likely because all of
                // the points we were given are coincident.
                this.storePoints(points, fromIndex, toIndex);
            }
        }
    }

    /**
     * Returns a reference to this node's child that contains points that
     * are closer to this node's center than this node's distance threshold.
     *
     * @return a reference to this node's "closer" child, or {@code null} if
     *         this is a leaf node
     *
     * @see VPNode#isLeafNode()
     */
    public VPNode<T> getCloserNode() {
        return this.closer;
    }

    /**
     * Returns a reference to this node's child that contains points that
     * are farther away from this node's center than this node's distance
     * threshold.
     *
     * @return a reference to this node's "farther" child, or {@code null}
     *         if this is a leaf node
     *
     * @see VPNode#isLeafNode()
     */
    public VPNode<T> getFartherNode() {
        return this.farther;
    }

    /**
     * Returns a point that is coincident with this node's center point.
     *
     * @return a point that is coincident with this node's center point
     */
    public VPPoint getCenter() {
        return new Kmer(this.center);
    }

    /**
     * Returns the distance threshold for this node if it is a non-leaf
     * node. Points that have a distance to this node's center less than or
     * equal to the distance threshold are stored in the "closer" child node
     * of this tree while points with a distance from the center greater
     * than the threshold are stored in the "farther" node.
     *
     * @return the distance threshold for this node
     *
     * @throws IllegalStateException if this node is a leaf node
     *
     * @see VPNode#getCenter()
     * @see VPNode#getCloserNode()
     * @see VPNode#getFartherNode()
     */
    public double getThreshold() {
        if(this.isLeafNode()) {
            throw new IllegalStateException("Leaf nodes do not have a" +
                    " distance threshold.");
        }

        return this.threshold;
    }

    /**
     * <p>Adds all of the points in a collection to this node (if it is a
     * leaf node) or its children. If this node is a leaf node and the added
     * points push this node beyond its capacity, it is partitioned as
     * needed after all points have been added.</p>
     *
     * <p>This method defers partitioning of child nodes until all points
     * have been added.</p>
     *
     * @param points
     *            the collection of points to add to this node or its
     *            children
     *
     * @return {@code true} if this node or its children were modified or
     *         {@code false} otherwise; vp-trees are always modified by the
     *         addition of points, so this method always returns
     *         {@code true} if {@code points} is not empty
     */
    public boolean addAll(Collection<? extends T> points) {
        HashSet<VPNode<T>> nodesAffected = new HashSet<VPNode<T>>();

        for(T point : points) {
            this.add(point, true, nodesAffected);
        }

        // Resolve all of the deferred maintenance
        for(VPNode<T> node : nodesAffected) {
            if(node.isOverloaded()) {
                try {
                    node.partition();
                } catch (PartitionException e) {
                    // Nothing to do here; this just means some nodes are
                    // bigger than they want to be.
                }
            } else {
                // We don't need to partition the node, but we may need to
                // trim it.
                node.points.trimToSize();
            }
        }

        // The tree was definitely modified as long as we were given a
        // non-empty collection of points to add.
        return !points.isEmpty();
    }

    /**
     * Adds a point to this node if it is a leaf node or one of its children
     * if not. If the node that ultimately holds the new point is loaded
     * beyond its capacity, it will be partitioned.
     *
     * @param point
     *            the point to add to this node or one of its children
     *
     * @return {@code true} if this node or one of its children was modified
     *         by the addition of this point or {@code false} otherwise
     */
    public boolean add(T point) {
        return this.add(point, false, null);
    }

    /**
     * <p>Adds a point to this node if it is a leaf node or one of its
     * children if not. If the node that ultimately holds the new point is
     * loaded beyond its capacity, it will be partitioned.</p>
     *
     * <p>Partitioning may optionally be deferred, in which case it is the
     * responsibility of the caller to partition overloaded nodes.</p>
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
     *            points; this may be {@code null} if
     *            {@code deferMaintenance} is {@code false}. Callers must
     *            use this set to partition or trim nodes later.
     *
     * @return {@code true} if this node or any of its children were
     *         modified by the addition of the new point or {@code false}
     *         otherwise; note that adding points always results in
     *         modification
     */
    protected boolean add(T point, boolean deferMaintenance,
                          Set<VPNode<T>> nodesAffected) {
        if(this.isLeafNode()) {
            this.points.add(point);

            if(deferMaintenance) {
                // We'll decide how to maintain this node later
                nodesAffected.add(this);
            } else {
                if(this.isOverloaded()) {
                    try {
                        this.partition();
                    } catch(PartitionException e) {
                        // Nothing to do here; just hold on to all of our
                        // points.
                    }
                } else {
                    // If we didn't need to partition, we may have some
                    // excess storage capacity. Trim our internal point
                    // store to keep memory overhead to a minimum.
                    this.points.trimToSize();
                }
            }
        } else {
            if(this.center.getDistanceTo(point) <= this.threshold) {
                return this.closer.add(point);
            } else {
                return this.farther.add(point);
            }
        }

        // There's no way to add a point and not modify the tree.
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
        if(this.isLeafNode()) {
            return this.points.contains(point);
        } else {
            if(this.center.getDistanceTo(point) <= this.threshold) {
                return this.closer.contains(point);
            } else {
                return this.farther.contains(point);
            }
        }
    }

    /**
     * Returns the number of points contained in this node and its child
     * nodes.
     *
     * @return the number of points in this node and its children
     */
    public int size() {
        if(this.isLeafNode()) {
            return this.points.size();
        } else {
            return this.closer.size() + this.farther.size();
        }
    }

    /**
     * Stores a subset of an array of points in this node directly, making
     * this node a leaf node.
     *
     * @param points
     *            the array of points from which to store a subset
     * @param fromIndex
     *            the starting index (inclusive) of the subset of the array
     *            to store
     * @param toIndex
     *            the end index (exclusive) of the subset of the array to
     *            store
     */
    private void storePoints(T[] points, int fromIndex, int toIndex) {
        this.points = new ArrayList<T>(toIndex - fromIndex);

        for(int i = fromIndex; i < toIndex; i++) {
            this.points.add(points[i]);
        }

        // Always choose a center point if we don't already have one
        if(this.center == null && !this.points.isEmpty()) {
            this.center = new Kmer(this.points.get(0));
        }

        this.closer = null;
        this.farther = null;
    }

    /**
     * Returns a collection of all the points stored directly in this node.
     *
     * @return a collection of all the points stored directly in this node
     *
     * @throws IllegalStateException if this node is not a leaf node
     */
    public Collection<T> getPoints() {
        if(!this.isLeafNode()) {
            throw new IllegalStateException("Cannot retrieve points from a" +
                    " non-leaf node.");
        }
        return new ArrayList<T>(this.points);
    }

    /**
     * Attempts to partition the points contained in this node into two
     * child nodes. Partitioning this node may trigger recursive
     * partitioning attempts in the generated child nodes.
     *
     * @throws PartitionException
     *             if this node is node a leaf node, if this node is empty,
     *             or if no viable distance threshold could be found (i.e.
     *             all points in this node have the same distance from the
     *             node's center)
     */
    public void partition() throws PartitionException {
        if(!this.isLeafNode()) {
            throw new PartitionException("Cannot partition a non-leaf node.");
        }

        if(!this.isEmpty()) {
            @SuppressWarnings("unchecked")
            T[] pointArray = this.points.toArray((T[]) Array.newInstance(
                    points.iterator().next().getClass(), 0));

            this.partition(pointArray, 0, pointArray.length);
        } else {
            throw new PartitionException("Cannot partition an empty node.");
        }
    }

    /**
     * Attempts to partition the points in a subset of the given array into
     * two child nodes based on their distance from the center of this node.
     * This method chooses a center point if none exists and chooses a
     * distance threshold to use as the criterion for node partitioning. The
     * threshold is chosen to be as close to the median distance of the
     * points in the sub-array as possible while still partitioning the
     * points into two groups. The child nodes generated by this method may
     * be partitioned recursively.
     *
     * @param points
     *            an array from which to partition a subset of points
     * @param fromIndex
     *            the start index of the sub-array of points to partition
     *            (inclusive)
     * @param toIndex
     *            the end index of the sub-array of points to partition
     *            (exclusive)
     *
     * @throws PartitionException
     *             if the range specified by {@code fromIndex} and
     *             {@code toIndex} includes fewer than two points or if no
     *             viable distance threshold could be found (i.e. all of the
     *             points in the subarray have the same distance from this
     *             node's center point)
     */
    protected void partition(T[] points, int fromIndex,
                             int toIndex) throws PartitionException {
        // We can't partition fewer then two points.
        if(toIndex - fromIndex < 2) {
            throw new PartitionException("Cannot partition fewer" +
                    " than two points.");
        }

        // We start by choosing a center point and a distance threshold; the
        // median distance from our center to points in our set is a safe
        // bet.
        if(this.center == null) {
            this.center = new Kmer(points[fromIndex]);
        }

        // TODO Consider optimizing this whole approach to partitioning
        java.util.Arrays.sort(points, fromIndex, toIndex);

        int medianIndex = (fromIndex + toIndex - 1) / 2;
        double medianDistance = this.center.getDistanceTo(points[medianIndex]);

        // Since we're picking a definite median value from the list, we're
        // guaranteed to have at least one point that is closer to or EQUAL TO
        // (via identity) the threshold; what we want to do now is make sure
        // there's at least one point that's farther away from the center
        // than the threshold.
        int partitionIndex = -1;

        for(int i = medianIndex + 1; i < toIndex; i++) {
            if(this.center.getDistanceTo(points[i]) > medianDistance) {
                // We found at least one point farther away than the median
                // distance. That means we can use the median as our
                // distance threshold and everything after that point in the
                // sorted array as members of the "farther" node.
                partitionIndex = i;
                this.threshold = medianDistance;

                break;
            }
        }

        // Did we find a point that's farther away than the median distance?
        // If so, great!
        //
        // If not, we know that all points after the median point in the
        // sorted array have the same distance from the center, and we need
        // to try to move the threshold back until we find a point that's
        // less distant than the median distance. If we find such a point,
        // we'll use its distance from the center as our distance threshold.
        // If the median distance is zero, though, and we've made it this
        // far, we know there's nothing MORE distant than that and shouldn't
        // spend time searching.
        if(partitionIndex == -1) {
            if(medianDistance != 0) {
                for(int i = medianIndex; i > fromIndex; i--) {
                    if(this.center.getDistanceTo(points[i]) < medianDistance) {
                        partitionIndex = i;
                        this.threshold = this.center.getDistanceTo(points[i]);

                        break;
                    }
                }

                // Did we still fail to find anything? There's still one
                // special case that can save us. If we've made it here, we
                // know that everything except the center point has the same
                // non-zero distance from the center. We can and should
                // still partition by putting the center alone in the
                // "closer" node and everything else in the "farther" node.
                // This, of course, assumes there's more than one point to
                // work with.
                if(partitionIndex == -1) {
                    partitionIndex = fromIndex + 1;
                }
            }
        }

        // Still nothing? Bail out.
        if(partitionIndex == -1) {
            throw new PartitionException(
                    "No viable partition threshold found (all points have" +
                            " equal distance from center).");
        }

        // Okay! Now actually use that partition index.
        this.closer = new VPNode<T>(points, fromIndex,
                partitionIndex, this.binSize);
        this.farther = new VPNode<T>(points, partitionIndex,
                toIndex, this.binSize);

        // We're definitely not a leaf node now, so clear out our internal
        // point ArrayList (if we had one).
        this.points = null;
    }

    /**
     * Tests whether this is a leaf node.
     *
     * @return {@code true} if this node is a leaf node or {@code false}
     *         otherwise
     */
    public boolean isLeafNode() {
        return this.closer == null;
    }

    /**
     * Tests whether this node and all of its children are empty.
     *
     * @return {@code true} if this node and all of its children contain no
     *         points or {@code false} otherwise
     */
    public boolean isEmpty() {
        if(this.isLeafNode()) {
            return this.points.isEmpty();
        } else {
            return (this.closer.isEmpty() && this.farther.isEmpty());
        }
    }

    /**
     * Tests whether this node contains more points than its maximum
     * capacity.
     *
     * @return {@code true} if the number of points stored in this node is
     *         greater than its capacity or {@code false} otherwise
     *
     * @throws IllegalStateException if this is not a leaf node
     */
    public boolean isOverloaded() {
        if(!this.isLeafNode()) {
            throw new IllegalStateException("Non-leaf nodes" +
                    " cannot be overloaded.");
        }

        return this.points.size() > this.binSize;
    }

    /**
     * Populates the given search result set with points close to the query
     * point. If this node is a leaf node, all of its contained points are
     * "offered" to the search result set as potential nearest neighbors. If
     * this is not a leaf node, one or both of its children are searched
     * recursively.
     *
     * @param queryPoint the point for which to find nearby neighbors
     * @param results the result set to which to offer points
     */
    public void getNearestNeighbors(final VPPoint queryPoint,
                                    BoundedPriorityQueue<T> results) {
        // If this is a leaf node, our job is easy. Offer all of our points
        // to the result set and bail out.
        if(this.isLeafNode()) {
            results.addAll(this.points);
        } else {
            // Descend through the tree recursively.
            boolean searchedCloserFirst;
            double distanceToCenter = this.center.getDistanceTo(queryPoint);

            if(distanceToCenter <= this.threshold) {
                this.closer.getNearestNeighbors(queryPoint, results);
                searchedCloserFirst = true;
            } else {
                this.farther.getNearestNeighbors(queryPoint, results);
                searchedCloserFirst = false;
            }

            // ...and now we're on our way back up. Decide if we need to search
            // whichever child we didn't search on the way down.
            if(searchedCloserFirst) {
                // We've already searched the node that contains points
                // within our threshold (which also implies that the query
                // point is inside our threshold); we also want to search
                // the node beyond our threshold if the distance from the
                // query point to the most distant match is longer than the
                // distance from the query point to our threshold, since
                // there could be a point outside our threshold that's
                // closer than the most distant match.
                double distanceToThreshold = this.threshold - distanceToCenter;

                if(results.getFurthestDistance() > distanceToThreshold) {
                    this.farther.getNearestNeighbors(queryPoint, results);
                }
            } else {
                // We've already searched the node that contains points
                // beyond our threshold, and the query point itself is
                // beyond our threshold. We want to search the
                // within-threshold node if it's "easier" to get from the
                // query point to our region than it is to get from the
                // query point to the most distant match, since there could
                // be a point within our threshold that's closer than the
                // most distant match.
                double distanceToThreshold = distanceToCenter - this.threshold;

                if(distanceToThreshold <= results.getFurthestDistance()) {
                    this.closer.getNearestNeighbors(queryPoint, results);
                }
            }
        }
    }

    /**
     * Adds all of the points from this node if it is a leaf node or its
     * children if it is not to an array. It is the responsibility of the
     * caller to ensure that the array has sufficient capacity to hold all
     * of the points in this node and its children.
     *
     * @param array
     *            the array to which to add points
     *
     * @see VPNode#size()
     */
    public void addPointsToArray(Object[] array) {
        this.addPointsToArray(array, 0);
    }

    /**
     * Adds all of the points from this node and its children to the given
     * array starting at the given offset. It is the responsibility of the
     * caller to ensure that the array has sufficient capacity to hold all
     * of the points in this node and its children.
     *
     * @param array
     *            the array to which to ad points
     * @param offset
     *            the starting index (inclusive) of the array to begin
     *            adding points
     *
     * @return the number of points added to the array
     */
    public int addPointsToArray(Object[] array, int offset) {
        if(this.isLeafNode()) {
            if(this.isEmpty()) { return 0; }

            System.arraycopy(this.points.toArray(), 0, array,
                    offset, this.points.size());

            return this.points.size();
        } else {
            int nAddedFromCloser = this.closer.addPointsToArray(array, offset);
            int nAddedFromFarther = this.farther.addPointsToArray(array,
                    offset + nAddedFromCloser);

            return nAddedFromCloser + nAddedFromFarther;
        }
    }

    /**
     * Finds the node at or below this node that contains (or would contain)
     * the given point. The given stack is populated with each node on the
     * path to the leaf node that contains the given point.
     *
     * @param p
     *            the point for which to find the containing node
     * @param stack
     *            the stack to populate with the chain of nodes leading to
     *            the leaf node that contains or would contain the given
     *            point
     */
    public void findNodeContainingPoint(final VPPoint p,
                                        final Deque<VPNode<T>> stack) {
        // First things first; add ourselves to the stack.
        stack.push(this);

        // If this is a leaf node, we don't need to do anything else. If
        // it's not a leaf node, recurse!
        if(!this.isLeafNode()) {
            if(this.center.getDistanceTo(p) <= this.threshold) {
                this.closer.findNodeContainingPoint(p, stack);
            } else {
                this.farther.findNodeContainingPoint(p, stack);
            }
        }
    }

    /**
     * Removes a point from this node's internal list of points.
     *
     * @param point
     *            the point to remove from this node
     *
     * @return {@code true} if the point was removed from this node (i.e.
     *         this node actually contained the given point) or
     *         {@code false} otherwise
     *
     * @throws IllegalStateException if this node is not a leaf node
     */
    public boolean remove(T point) {
        if(this.isLeafNode()) {
            boolean pointRemoved = this.points.remove(point);

            if(pointRemoved) { this.points.trimToSize(); }

            return pointRemoved;
        } else {
            throw new IllegalStateException("Cannot remove points from" +
                    " a non-leaf node.");
        }
    }

    /**
     * Recursively absorbs the points contained in this node's children into
     * this node, making this node a leaf node in the process.
     *
     * @throws IllegalStateException
     *             if this node is a leaf node (and thus has no children)
     */
    public void absorbChildren() {
        if(this.isLeafNode()) {
            throw new IllegalStateException("Leaf nodes have no children.");
        }

        this.points = new ArrayList<T>(this.size());

        if(!this.closer.isLeafNode()) {
            this.closer.absorbChildren();
        }

        if(!this.farther.isLeafNode()) {
            this.farther.absorbChildren();
        }

        this.points.addAll(this.closer.getPoints());
        this.points.addAll(this.farther.getPoints());

        this.closer = null;
        this.farther = null;
    }

    /**
     * Populates the given {@code List} with all of the leaf nodes that are
     * descendants of this node.
     *
     * @param leafNodes the list to populate with leaf nodes
     */
    public void gatherLeafNodes(List<VPNode<T>> leafNodes) {
        if(this.isLeafNode()) {
            leafNodes.add(this);
        } else {
            this.closer.gatherLeafNodes(leafNodes);
            this.farther.gatherLeafNodes(leafNodes);
        }
    }

    /**
     * Tests whether this node is an ancestor of the given node.
     *
     * @param node
     *            the node for which to test ancestry
     *
     * @return {@code true} if the given node is a descendant of this node
     *         or {@code false} otherwise
     */
    public boolean isAncestorOfNode(VPNode<T> node) {
        // Obviously, leaf nodes can't be the ancestors of anything
        if(this.isLeafNode()) { return false; }

        // Find a path to the center of the node we've been given
        ArrayDeque<VPNode<T>> stack = new ArrayDeque<VPNode<T>>();
        this.findNodeContainingPoint(node.getCenter(), stack);

        // We're an ancestor if we appear anywhere in the path to the given
        // node
        return stack.contains(this);
    }
}