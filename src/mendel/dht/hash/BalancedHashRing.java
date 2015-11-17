/*
Copyright (c) 2015, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package mendel.dht.hash;

import mendel.data.Metadata;
import mendel.vptree.types.ProteinSequence;

import java.math.BigInteger;
import java.util.TreeMap;

/**
 * Creates an evenly-spaced hash ring network topology.  Note that nodes in the
 * topology are spaced evenly apart, but of course this does not guarantee the
 * data will hash uniformly.
 *
 * @author malensek
 */
public class BalancedHashRing<T> implements HashRing<T> {

    /** If set to True, the first position in the hash ring is randomized. */
    protected boolean randomize = false;

    protected HashFunction<T> function;
    protected BigInteger maxHash;

    /** Maps hash ring positions to ring entries */
    protected TreeMap<BigInteger, HashRingEntry> entryMap = new TreeMap<>();

    /**
     * Creates a BalancedHashRing using the provided hash function.  The
     * function will determine the size of the hash space and where nodes will
     * be placed in the topology.
     *
     * @param function HashFunction that defines the hash space being used.
     */
    public BalancedHashRing(HashFunction<T> function) {
        this(function, false);
    }

    /**
     * Creates a BalancedHashRing using the provided hash function.  The
     * function will determine the size of the hash space and where nodes will
     * be placed in the topology.
     *
     * @param function  HashFunction that defines the hash space being used.
     * @param randomize true if the first node position in the hash ring is
     *                  randomized, otherwise first node position at 0
     */
    public BalancedHashRing(HashFunction<T> function, boolean randomize) {
        this.function = function;
        this.randomize = randomize;
        maxHash = function.maxValue();
    }

    /**
     * Insert a node into the hash ring internal data structures.  Nodes are
     * relinked with the proper neighbors.
     *
     * @param position place in the hash space
     * @param predecessor the predecessor node in the hash space.
     */
    private void addRingEntry(BigInteger position, HashRingEntry predecessor)
    throws HashTopologyException {
        if (entryMap.get(position) != null) {
            /* Something is already here! */
            System.out.println(position);
            throw new HashTopologyException("Hash space exhausted!");
        }

        HashRingEntry newEntry
            = new HashRingEntry(position, predecessor.neighbor);
        predecessor.neighbor = newEntry;
        entryMap.put(position, newEntry);
    }

    /**
     * Add a node to the overlay network topology.
     *
     * @param data unused for this hash ring; nodes are placed evenly based on
     * current topology characteristics.  You may safely pass 'null' to this
     * method.
     *
     * @return the location of the new node in the hash space.
     */
    @Override
    public BigInteger addNode(T data)
    throws HashTopologyException, HashException {
        /* Edge case: when there are no entries in the hash ring yet. */
        if (entryMap.values().size() == 0) {
            BigInteger pos;

            if (randomize) {
                /* Find a random location to start with */
                pos = function.randomHash();
            } else {
                pos = BigInteger.ZERO;
            }

            HashRingEntry firstEntry = new HashRingEntry(pos);
            entryMap.put(pos, firstEntry);

            return pos;
        }

        /* Edge case: only one entry in the hash ring */
        if (entryMap.values().size() == 1) {
            HashRingEntry firstEntry = entryMap.values().iterator().next();
            BigInteger halfSize = maxHash.divide(BigInteger.valueOf(2));
            BigInteger secondPos = firstEntry.position.add(halfSize);

            if (secondPos.compareTo(maxHash) > 0) {
                secondPos = secondPos.subtract(maxHash);
            }

            HashRingEntry secondEntry
                = new HashRingEntry(secondPos, firstEntry);
            firstEntry.neighbor = secondEntry;
            entryMap.put(secondPos, secondEntry);

            return secondPos;
        }

        /* Find the largest empty span of hash space */
        BigInteger largestSpan = BigInteger.ZERO;
        HashRingEntry largestEntry = null;
        for (HashRingEntry entry : entryMap.values()) {
            BigInteger len = lengthBetween(entry, entry.neighbor);
            if (len.compareTo(largestSpan) > 0) {
                largestSpan = len;
                largestEntry = entry;
            }
        }

        if (largestEntry == null) {
            return BigInteger.ONE.negate();
        }

        /* Put the new node in the middle of the largest span */
        BigInteger half = half(largestEntry, largestEntry.neighbor);
        addRingEntry(half, largestEntry);
        return half;
    }

    /**
     * Find the hash location in the middle of a hash span.  When the hash space
     * is represented as a continuous circle, a span begins at an arbitrary
     * starting point and then proceeds clockwise until it reaches its endpoint.
     *
     * @param start beginning of the hash span
     * @param end end of the hash span
     *
     * @return hash position in the middle of the span.
     */
    private BigInteger half(HashRingEntry start, HashRingEntry end) {
        BigInteger length = lengthBetween(start, end);
        /* half = start + length / 2 */
        BigInteger half
            = start.position.add(length.divide(BigInteger.valueOf(2)));

        if (maxHash.compareTo(half) >= 0) {
            return half;
        } else {
            return half.subtract(maxHash);
        }
    }

    /**
     * Determines the number of hash values between two positions on a hash
     * ring.  The hash space is viewed as a ring, starting with 0 and proceeding
     * clockwise until it reaches its maximum value.  A hash span starts at an
     * arbitrary position, and then proceeds clockwise until it reaches its
     * ending position.  The number of values between the two points is the hash
     * span length.
     *
     * @param start the start of the hash span being measured
     * @param end the end of the hash span being measured
     *
     * @return the length of the hash span
     */
    private BigInteger lengthBetween(HashRingEntry start, HashRingEntry end) {
        BigInteger difference = end.position.subtract(start.position);

        if (difference.compareTo(BigInteger.ZERO) >= 0) {
            return difference;
        } else {
            /* Wraparound */
            /* wrapped = (MAX_HASH - start) + end */
            BigInteger wrapped
                = maxHash.subtract(start.position).add(end.position);
            return wrapped;
        }
    }

    @Override
    public BigInteger locate(T data) throws HashException {
        BigInteger hashLocation = function.hash(data);
        BigInteger node = entryMap.ceilingKey(hashLocation);

        /* Wraparound edge case */
        if (node == null) {
            node = entryMap.ceilingKey(BigInteger.ZERO);
        }

        return node;
    }

    /**
     * Formats the hash ring as node-to-predecessor pair Strings.
     *
     * @return network links and nodes in String format.
     */
    @Override
    public String toString() {
        String str = "";
        HashRingEntry firstEntry = entryMap.values().iterator().next();
        HashRingEntry currentEntry = firstEntry;
        HashRingEntry nextEntry;

        do {
            nextEntry = currentEntry.neighbor;
            str += currentEntry.position + " -> " + nextEntry.position;
            str += System.lineSeparator();
            currentEntry = nextEntry;
        } while (currentEntry != firstEntry);

        return str;
    }

    public static void main(String[] args) throws HashException, HashTopologyException {
        BalancedHashRing<Metadata> ring = new BalancedHashRing<>(new SHA1());
        for (int i = 0; i < 30; i++) {
            ring.addNode(null);
        }

        ProteinSequence k1 = new ProteinSequence("ATATATAATTATAT");
        ProteinSequence k2 = new ProteinSequence("ATATATAATTATAC");
        ProteinSequence k3 = new ProteinSequence("ACATGCTGCCGAGC");
        Metadata m1 = new Metadata(k1, "t1");
        Metadata m2 = new Metadata(k1, "t2");
        Metadata m3 = new Metadata(k2, "t1");
        Metadata m4 = new Metadata(k3, "t1");

        System.out.println(ring.locate(m1));
        System.out.println(ring.locate(m2));
        System.out.println(ring.locate(m3));
        System.out.println(ring.locate(m4));

    }
}
