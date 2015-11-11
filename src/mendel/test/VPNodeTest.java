package mendel.test;

import mendel.vptree.types.Sequence;
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
        VPNode<Sequence> test = new VPNode<>(10, 0, 0);
        /* Create list of k-mers */
        Sequence[] array = new Sequence[7];
        array[4] = new Sequence("AAAAAAA");
        array[1] = new Sequence("AAAAAAC");
        array[2] = new Sequence("AAAAACC");
        array[5] = new Sequence("AAAACCC");
        array[0] = new Sequence("AAACCCC");
        array[3] = new Sequence("ACCCCCC");
        array[6] = new Sequence("AACCCCC");

        int lower = 0, upper = 6, n = array.length/2;
        test.nth_element(array, lower, upper, n, array[4]);
        assertEquals(new Sequence("AAAACCC").toString(), array[5].toString());
    }
}