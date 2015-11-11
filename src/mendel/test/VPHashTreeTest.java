package mendel.test;

import mendel.data.Metadata;
import mendel.dht.StorageNode;
import mendel.dht.hash.HashException;
import mendel.dht.hash.HashTopologyException;
import mendel.dht.partition.PartitionException;
import mendel.dht.partition.PartitionerException;
import mendel.dht.partition.VPHashPartitioner;
import mendel.network.GroupInfo;
import mendel.network.NetworkInfo;
import mendel.network.NodeInfo;
import mendel.vptree.types.ProteinSequence;

import java.util.*;


public class VPHashTreeTest {
    public VPHashPartitioner partitioner;

    private final char[] chars = {'C', 'S', 'T', 'P', 'A', 'G', 'N', 'D', 'E',
            'Q', 'H','R', 'K', 'M', 'I', 'L', 'V', 'F', 'Y', 'W'};

    public static void main(String args[]) throws Exception, PartitionException {
        VPHashTreeTest test = new VPHashTreeTest();

        Metadata data1 = new Metadata(
                new ProteinSequence("CIAKMKIGADD"), "name1");
        Metadata data2 = new Metadata(
                new ProteinSequence("ALKIDALKIFA"), "name2");

        HashMap<String, Integer> loadBalance = new HashMap<>();


        Random rand = new Random(0);
        for (int i = 0; i < 1500; i++) {
            String seq = "";
            for (int j = 0; j < 10; j++) {
                seq += test.chars[rand.nextInt(20)];
            }
            Metadata tempData = new Metadata(
                    new ProteinSequence(seq), "test_" + i);
            String node = test.partitioner.locateData(tempData).toString();
            Integer val = loadBalance.get(node);
            if (loadBalance.get(node) == null) {
                loadBalance.put(node, 1);
            } else {
                loadBalance.put(node, val + 1);
            }
        }

        int total = 0;
        for (Map.Entry<String, Integer> entry : loadBalance.entrySet()) {
            System.out.println(entry);
            total += entry.getValue();
        }
        System.out.println("total: " + total);
    }

    public VPHashTreeTest() throws HashException,
            HashTopologyException, PartitionerException {

        /* Create our fake DHT ring with 3 groups of 5 */
        NetworkInfo network = new NetworkInfo();
        for (int i = 0; i < 3; i++) {
            GroupInfo group = new GroupInfo("Group " + i);
            for (int j = 0; j < 5; j++) {
                group.addNode(new NodeInfo("Group " + i + ", host " + j, 0));
            }
            network.addGroup(group);
        }
        partitioner = new VPHashPartitioner(new StorageNode(true), network);

        /* Generate some data: 50 length 10 protein sequences */
        ArrayList<ProteinSequence> list = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 500; i++) {
            String seq = "";
            for (int j = 0; j < 10; j++) {
                seq += chars[rand.nextInt(4)];
            }
            ProteinSequence protSeq = new ProteinSequence(seq);
            list.add(protSeq);
        }
        /* Add the data to the test index */
        partitioner.updateIndex(list);
    }
}
