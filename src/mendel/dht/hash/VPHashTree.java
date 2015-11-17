/*
 * Copyright (c) 2015, Colorado State University All rights reserved.
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

package mendel.dht.hash;

import mendel.data.Metadata;
import mendel.vptree.VPPoint;
import mendel.vptree.VPTree;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class VPHashTree extends VPTree<VPPoint>
        implements HashFunction<Metadata> {

    /* tree height to draw the cutoff */
    int depth;

    /* No lookup can be performed until the depth is reached */
    private boolean depthReached;

    /**
     * Constructs a new, empty vp-tree with a default node capacity.
     */
    public VPHashTree(int depth) {
        super(DEFAULT_BIN_SIZE);
        this.depth = depth;
        this.depthReached = false;
    }

    /**
     * Constructs a new, empty vp-tree with the specified node capacity.
     *
     * @param depth         height of the tree where to draw the cutoff for
     *                      hashing paths
     * @param nodeCapacity  the maximum number of points to store in a leaf node
     *                      of the vp-tree
     */
    public VPHashTree(int depth, int nodeCapacity) {
        super(nodeCapacity);
        this.depth = depth;
        this.depthReached = false;
    }

    /**
     * Constructs a new vp-tree that contains (and indexes) all of the points in
     * the given collection. Nodes of the vp-tree are created with a default
     * capacity.
     *
     * @param depth     height of the tree where to draw the cutoff for
     *                  hashing paths
     * @param points    the points to use to populate this vp-tree
     */
    public VPHashTree(int depth, Collection<? extends VPPoint> points) {
        super(points, DEFAULT_BIN_SIZE);
        this.depth = depth;
        this.depthReached = false;
    }

    /**
     * Constructs a new vp-tree that contains (and indexes) all of the points in
     * the given collection and has leaf nodes with the given point capacity.
     *
     * @param depth         height of the tree where to draw the cutoff for
     *                      hashing paths
     * @param points        the points to use to populate this vp-tree
     * @param nodeCapacity  the largest number of points any leaf node of the
     */
    public VPHashTree(int depth, ArrayList<? extends VPPoint> points,
                      int nodeCapacity) {
        super(points, nodeCapacity);
        this.depth = depth;
        this.depthReached = false;
    }

    /**
     * Finds the prefix value in the hash tree of the specified @code{VPPoint}.
     * @param value the value to find a prefix for
     * @return  the prefix of the node where the value exists.
     * @throws HashException    when the tree does not contain enough nodes to
     *                          reach the depth threshold
     */
    public long lookup(VPPoint value) throws HashException {
        long retval = getPrefixOf(value, depth);
        return retval;
    }

    /**
     * Hashes the metadata in the tree.
     * @param metadata  the @code{Metadata} containing the @code{VPPoint} to get
     *                  a hash value for
     * @return  the SHA-1 hash of the prefix of the specified @code{VPPoint}
     * @throws HashException    when the tree does not contain enough nodes to
     *                          reach the depth threshold
     */
    @Override
    public BigInteger hash(Metadata metadata) throws HashException {
        SHA1 SHA1hash = new SHA1();
        VPPoint value = metadata.getSegment();
        long prefix = lookup(value);
        return SHA1hash.hash(prefix);
    }

    @Override
    public BigInteger maxValue() {
        return new SHA1().maxValue();
    }

    @Override
    public BigInteger randomHash() throws HashException {
        Random random = new Random();
        return BigInteger.valueOf(random.nextLong());
    }

    public String getHashTreeDOT() {
        return generateDot();
    }
}
