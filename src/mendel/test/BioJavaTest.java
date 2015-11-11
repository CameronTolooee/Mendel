package mendel.test;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava.nbio.core.sequence.io.FastaReader;
import org.biojava.nbio.core.sequence.io.GenericFastaHeaderParser;
import org.biojava.nbio.core.sequence.io.ProteinSequenceCreator;

/**
 * Created by ctolooee on 7/13/15.
 */
public class BioJavaTest {
    private static final String BASE_PATH = "data/astral_2.05_40.fa";
    public static void main(String[] args) throws IOException {

        //Try reading with the FastaReader
        FileInputStream inStream = new FileInputStream(BASE_PATH);
        FastaReader<ProteinSequence,AminoAcidCompound> fastaReader =
                new FastaReader<>(inStream,
                        new GenericFastaHeaderParser<ProteinSequence,AminoAcidCompound>(),
                        new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
        LinkedHashMap<String, ProteinSequence> b = fastaReader.process();
        for (  Map.Entry<String, ProteinSequence> entry : b.entrySet() ) {
            System.out.println(entry.getValue().getOriginalHeader());
        }
    }
}
