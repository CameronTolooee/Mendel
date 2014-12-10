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

package mendel.dht.partition;

import mendel.data.Metadata;
import mendel.dht.StorageNode;
import mendel.dht.hash.BalancedHashRing;
import mendel.dht.hash.HashException;
import mendel.dht.hash.HashRing;
import mendel.dht.hash.HashTopologyException;
import mendel.dht.hash.VPHashTree;
import mendel.network.GroupInfo;
import mendel.network.NetworkInfo;
import mendel.network.NodeInfo;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class VPHashPartitioner extends Partitioner<Metadata> {


    private Logger logger = Logger.getLogger("mendel");

    private BalancedHashRing<Metadata> groupHashRing;
    private Map<BigInteger, GroupInfo> groupPositions = new HashMap<>();

    private VPHashTree hash = new VPHashTree();

    private HashMap<BigInteger, BalancedHashRing<Metadata>> nodeHashRings
            = new HashMap<>();
    private Map<BigInteger, Map<BigInteger, NodeInfo>> nodePositions
            = new HashMap<>();


    public VPHashPartitioner(StorageNode storageNode, NetworkInfo network)
            throws PartitionerException, HashTopologyException, HashException {
        super(storageNode, network);

        List<GroupInfo> groups = network.getGroups();
        if (groups.size() == 0) {
            throw new PartitionerException("At least one group must exist in " +
                    "the network configuration.");
        }

        groupHashRing = new BalancedHashRing<>(hash);

        for (GroupInfo group : groups) {
            placeGroup(group);
        }
    }

    public String getMetadataTreeDOT() {
        return hash.getHashTreeDOT();
    }

    private void placeGroup(GroupInfo group) throws HashException,
            HashTopologyException {
        BigInteger position = groupHashRing.addNode(null);
        groupPositions.put(position, group);
        logger.info(String.format("Group '%s' placed at %d",
                group.getName(), position));

        nodeHashRings.put(position, new BalancedHashRing<>(hash));
        for (NodeInfo node : group.getNodes()) {
            placeNode(position, node);
        }
    }

    private void placeNode(BigInteger groupPosition, NodeInfo node) throws
            HashException, HashTopologyException {
        BalancedHashRing<Metadata> hashRing = nodeHashRings.get(groupPosition);
        BigInteger nodePosition = hashRing.addNode(null);

        GroupInfo group = groupPositions.get(groupPosition);

        logger.info(String.format("Node [%s] placed in Group '%s' at %d",
                node, group.getName(), nodePosition));

        if (nodePositions.get(groupPosition) == null) {
            nodePositions.put(groupPosition,
                    new HashMap<BigInteger, NodeInfo>());
        }
        nodePositions.get(groupPosition).put(nodePosition, node);
    }

    @Override
    public NodeInfo locateData(Metadata metadata)
            throws HashException, PartitionException {

        /* First, determine the group that should hold this file */
        BigInteger group = groupHashRing.locate(metadata);

        /* Next, the StorageNode */
        HashRing<Metadata> nodeHash = nodeHashRings.get(group);
        BigInteger node = nodeHash.locate(metadata);
        NodeInfo info = nodePositions.get(group).get(node);
        if (info == null) {
            throw new PartitionException("Could not locate specified data");
        }
        return info;
    }
}
