package mendel.util;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.FractionalSimilarityScorer;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.SimpleSubstitutionMatrix;
import org.biojava.nbio.alignment.template.AlignedSequence;
import org.biojava.nbio.alignment.template.SequencePair;
import org.biojava.nbio.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.AccessionID;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;

import java.util.List;

public class SmithWaterman implements Comparable<SmithWaterman> {

    private String qID, sID, query, subject;
    private int gapPenalty;
    private double score;
    private SequencePair<ProteinSequence, AminoAcidCompound> alignment;


    public SmithWaterman(String queryID, String query,
                         String subjectID, String subject)
            throws CompoundNotFoundException {
        this.qID = queryID;
        this.sID = subjectID;
        this.query = query;
        this.subject = subject;
        swAlignment();
    }

    private void  swAlignment() throws CompoundNotFoundException {

        ProteinSequence s1 = new ProteinSequence(query);
        s1.setAccession(new AccessionID("Query"));

        ProteinSequence s2 = new ProteinSequence(subject);
        s2.setAccession(new AccessionID("Subject"));

        SubstitutionMatrix<AminoAcidCompound> matrix
                = SimpleSubstitutionMatrix.getBlosum62();


        alignment =  Alignments.getPairwiseAlignment(s1, s2,
                        Alignments.PairwiseSequenceAlignerType.LOCAL,
                        new SimpleGapPenalty(), matrix);

        FractionalSimilarityScorer<ProteinSequence, AminoAcidCompound> scorer =
                new FractionalSimilarityScorer<>(alignment);
        score = scorer.getScore();
    }

    public double getScore() {
        return score;
    }

    public String getSubjectID() {
        return sID;
    }

    public SequencePair<ProteinSequence, AminoAcidCompound> getAlignment() {
        return alignment;
    }

    public String toString() {
        String str = "";
        List<AlignedSequence<ProteinSequence, AminoAcidCompound>>
                alignedSequences = alignment.getAlignedSequences();

        AlignedSequence<ProteinSequence, AminoAcidCompound> align1 =
                alignedSequences.get(0);

        AlignedSequence<ProteinSequence, AminoAcidCompound> align2 =
                alignedSequences.get(1);

        String val1 = align1.getSequenceAsString();
        String val2 = align2.getSequenceAsString();
        String subVal;
        int pos1 = align1.getStart().getPosition();
        int pos2 = align2.getStart().getPosition();

        while (val1.length() > 60) {
            str += pos1 + "\t";
            pos1 += 60;
            subVal = val1.substring(0, 60);
            str += subVal + "\t" + pos1 + "\n";

            str += "\t";
            for(int i = 0; i < 60; ++i) {
                if(val1.charAt(i) == val2.charAt(i)) {
                    str += val1.charAt(i);
                } else if (val1.charAt(i) == '-' || val2.charAt(i) == '-') {
                    str += " ";
                } else {
                    SubstitutionMatrix<AminoAcidCompound> matrix
                            = SimpleSubstitutionMatrix.getBlosum62();
                    str += "+";
                }
            }
            str += "\n";

            str += pos2 + "\t";
            pos2 += 60;
            subVal = val2.substring(0, 60);
            str += subVal + "\t" + pos2 + "\n\n";

            val1 = val1.substring(60);
            val2 = val2.substring(60);
        }
        str += pos1 + "\t" + val1 + "\t" + (pos1 + val1.length()) + "\n";
        str += "\t";
        for(int i = 0; i < val1.length(); ++i) {
            if(val1.charAt(i) == val2.charAt(i)) {
                str += val1.charAt(i);
            } else if (val1.charAt(i) == '-' || val2.charAt(i) == '-') {
                str += " ";
            } else {
                str += "+";
            }
        }
        str += "\n";
        str += pos2 + "\t" + val2 + "\t" + (pos2 + val2.length()) + "\n\n";
        return str;
    }

    @Override
    public int compareTo(SmithWaterman o) {
        return (int) o.score - (int) this.score;
    }
}