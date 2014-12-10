package mendel.test;

import mendel.data.Metadata;
import mendel.dht.hash.VPHashTree;
import mendel.vptree.Kmer;
import mendel.vptree.VPTree;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class VPHashTreeTest {
    private VPTree<Kmer> vpTree;

    @Before
    public void setup() {
        /* Create list of k-mers */
        ArrayList<Kmer> list = new ArrayList<>();
        list.add(new Kmer("ACTGCCTGA"));
        list.add(new Kmer("ACTTCCTGA"));
        list.add(new Kmer("ACTCCCTGA"));
        list.add(new Kmer("AAGGCCTGA"));
        list.add(new Kmer("GGTGCCTGA"));
        list.add(new Kmer("CGTGATGCA"));
        list.add(new Kmer("ACCCCCCCC"));
        list.add(new Kmer("AAAAAAACC"));
        list.add(new Kmer("AAAAAACCC"));
        list.add(new Kmer("AAAAACCCC"));
        list.add(new Kmer("AAAACCCCC"));
        list.add(new Kmer("AAACCCCCC"));
        list.add(new Kmer("ACCCCCCCC"));
        vpTree = new VPTree<>(list);
    }

    @Test
    public void testLookup() throws Exception {
        long v1 = vpTree.add(new Kmer("CCCCCCCCC"));
        VPHashTree hashTree = new VPHashTree(vpTree);
        Metadata data = new Metadata("CCCCCCCCC","name1");


        BigInteger v2 = hashTree.hash(data);
        assertEquals(BigInteger.valueOf(v1), v2);
    }

}