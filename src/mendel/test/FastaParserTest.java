package mendel.test;

import mendel.data.parse.FastaParser;
import mendel.vptree.types.ProteinSequence;
import mendel.vptree.types.Sequence;

import java.io.FileNotFoundException;
import java.util.Iterator;


public class FastaParserTest {

    public static void main(String[] args) throws FileNotFoundException {
        if(args.length < 1) {
            System.out.println("usage: mendel.test.FastaParserTest filename");
            System.exit(1);
        }

        FastaParser parser = new FastaParser(args[0]);
        Iterator<ProteinSequence> contigParser = parser.windowIterator();
        while (contigParser.hasNext()) {
            System.out.println("----------------------------------------");
            System.out.println(contigParser.next());
        }
    }
}