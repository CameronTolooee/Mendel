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

import mendel.config.SystemConfig;
import mendel.data.Metadata;
import mendel.dht.StorageNode;
import mendel.dht.hash.BalancedHashRing;
import mendel.dht.hash.HashException;
import mendel.dht.hash.HashRing;
import mendel.dht.hash.HashTopologyException;
import mendel.dht.hash.SHA1;
import mendel.dht.hash.VPHashTree;
import mendel.network.GroupInfo;
import mendel.network.NetworkDestination;
import mendel.network.NetworkInfo;
import mendel.network.NodeInfo;
import mendel.util.FileNames;
import mendel.util.Pair;
import mendel.vptree.types.ProteinSequence;
import mendel.vptree.types.Sequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava.nbio.core.sequence.io.FastaReader;
import org.biojava.nbio.core.sequence.io.GenericFastaHeaderParser;
import org.biojava.nbio.core.sequence.io.ProteinSequenceCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;

public class VPHashPartitioner extends Partitioner<Metadata> {

    private Logger logger = Logger.getLogger("mendel");
    private final int DEPTH = 2;
    private final int NODE_CAP = 25;

    private SHA1 nodeHash = new SHA1();
    private VPHashTree groupHash;

    private BalancedHashRing<Metadata> groupHashRing;
    private Map<BigInteger, GroupInfo> groupPositions = new HashMap<>();

    private Map<BigInteger, BalancedHashRing<Metadata>> nodeHashRing
            = new HashMap<>();
    private Map<BigInteger, Map<BigInteger, NodeInfo>> nodePositions
            = new HashMap<>();


    public VPHashPartitioner(StorageNode storageNode, NetworkInfo network)
            throws PartitionerException, HashTopologyException, HashException {
        super(storageNode, network);

        List<GroupInfo> groups = network.getGroups();

        if (groups.size() == 0) {
            throw new PartitionerException("At least one group must exist in "
                    + "the current network configuration to use this "
                    + "partitioner.");
        }

        groupHash = new VPHashTree(DEPTH, NODE_CAP); // TODO REVERT NODE CAPACITY

        groupHashRing = new BalancedHashRing<>(groupHash);

        for (GroupInfo group : groups) {
            placeGroup(group);
        }
    }

    private void placeGroup(GroupInfo group)
            throws HashException, HashTopologyException {
        BigInteger position = groupHashRing.addNode(null);
        groupPositions.put(position, group);
        logger.info(String.format("Group '%s' placed at %d",
                group.getName(), position));

        nodeHashRing.put(position, new BalancedHashRing<>(nodeHash));
        for (NodeInfo node : group.getNodes()) {
            placeNode(position, node);
        }
    }

    private void placeNode(BigInteger groupPosition, NodeInfo node)
            throws HashException, HashTopologyException {
        BalancedHashRing<Metadata> hashRing = nodeHashRing.get(groupPosition);
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

    public void updateIndex(Sequence data) {
        groupHash.add(data);
    }

    public void updateIndex(List<ProteinSequence> data) {
        groupHash.addAll(data);
    }

/* LOCATION LOOKUP FOR A FLAT HASHING ARCHITECTURE
    @Override
    public NodeInfo locateData(Metadata metadata)
            throws HashException, PartitionException {
        BigInteger node = nodeHashRing.locate(metadata);
        NodeInfo info = nodePositions.get(node);
        if (info == null) {
            throw new PartitionException("Could not locate specified data");
        }
        return info;
    }
*/

    public String generateDOT() {
        return groupHash.generateDot();
    }

    @Override
    public NodeInfo locateData(Metadata metadata)
            throws HashException, PartitionException {

        /* First, determine the group that should have this sequence */
        // TODO Branching paths in the lookup
        BigInteger group = groupHashRing.locate(metadata);

        /* Next, find the StorageNode within the group */
        HashRing<Metadata> nodeHash = nodeHashRing.get(group);
        BigInteger node = nodeHash.locate(metadata);
        NodeInfo info = nodePositions.get(group).get(node);
        if (info == null) {
            throw new PartitionException("Could not locate specified data");
        }
        return info;
    }

    public void stageData() throws IOException {
        List<mendel.vptree.types.ProteinSequence> batch = new ArrayList<>();
        String dataDir = SystemConfig.getStagedDataDir();
        File dir = new File(dataDir);
        for (File f : dir.listFiles()) {
            Pair<String, String> nameParts = FileNames.splitExtension(f);
            String ext = nameParts.b;
            if (ext.equals("fa") || ext.equals("fas") || ext.equals("seq")
                    || ext.equals("fasta") || ext.equals("fsa")) {

                FileInputStream inStream = new FileInputStream(f);
                FastaReader<org.biojava.nbio.core.sequence.ProteinSequence, AminoAcidCompound> fastaReader =
                        new FastaReader<>(inStream,
                                new GenericFastaHeaderParser<>(),
                                new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
                LinkedHashMap<String, org.biojava.nbio.core.sequence.ProteinSequence> b = fastaReader.process();
                int window_size = SystemConfig.getWindowSize();
                for (Map.Entry<String, org.biojava.nbio.core.sequence.ProteinSequence> entry : b.entrySet()) {
                    String seq = entry.getValue().getSequenceAsString();

                    int len = seq.length();
                    for (int i = 0; i + window_size < len; ++i) {
                        while (i + window_size < len && batch.size() < 800) {
                            batch.add(new mendel.vptree.types.ProteinSequence(seq.substring(i, i + window_size)));
                            ++i;
                        }
                        groupHash.addAll(batch);
                        batch.clear();
                    }
                }
            } else {
                logger.warning("Staged data file not read: incorrect format. "
                        + f.getName());
            }
        }
    }
}
