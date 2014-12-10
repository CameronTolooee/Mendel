package mendel.test;

import mendel.vptree.Kmer;
import mendel.vptree.VPNode;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class VPNodeTest {

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testNth_element() throws Exception {
        VPNode<Kmer> test = new VPNode<>(10, 0);
        /* Create list of k-mers */
        Kmer[] array = new Kmer[7];
        array[4] = new Kmer("AAAAAAA");
        array[1] = new Kmer("AAAAAAC");
        array[2] = new Kmer("AAAAACC");
        array[5] = new Kmer("AAAACCC");
        array[0] = new Kmer("AAACCCC");
        array[3] = new Kmer("ACCCCCC");
        array[6] = new Kmer("AACCCCC");

        int lower = 0, upper = 6, n = array.length/2;
        test.nth_element(array, lower, upper, n, array[4]);
        assertEquals(new Kmer("AAAACCC").toString(), array[5].toString());
    }
}