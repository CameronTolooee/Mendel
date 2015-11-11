/*
 * Copyright (c) 2015, Colorado State University All rights reserved.
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

package mendel.util;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.SimpleSubstitutionMatrix;
import org.biojava.nbio.alignment.template.SequencePair;
import org.biojava.nbio.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;



import java.net.URL;

public class ProteinAlignment {
    public static void main(String[] args) {
        String[] ids = new String[] {"Q21691", "Q21495", "O48771"};
        try {
            alignPairLocal(ids[0], ids[1]);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void alignPairLocal(String id1, String id2) throws Exception {
        ProteinSequence s1 = new ProteinSequence("METSSSLPLSPISIEPEQPSHRDYDITTRRGVGTTGNPIELCTNHFNVSVRQPDVVFYQY" +
                "TVSITTENGDAVDGTGISRKLMDQLFKTYSSDLDGKRLAYDGEKTLYTVGPLPQNEFDFL" +
                "VIVEGSFSKRDCGVSDGGSSSGTCKRSKRSFLPRSYKVQIHYAAEIPLKTVLGTQRGAYT" +
                "PDKSAQDALRVLDIVLRQQAAERGCLLVRQAFFHSDGHPMKVGGGVIGIRGLHSSFRPTH" +
                "GGLSLNIDVSTTMILEPGPVIEFLKANQSVETPRQIDWIKAAKMLKHMRVKATHRNMEFK" +
                "IIGLSSKPCNQQLFSMKIKDGEREVPIREITVYDYFKQTYTEPISSAYFPCLDVGKPDRP" +
                "NYLPLEFCNLVSLQRYTKPLSGRQRVLLVESSRQKPLERIKTLNDAMHTYCYDKDPFLAG" +
                "CGISIEKEMTQVEGRVLKPPMLKFGKNEDFQPCNGRWNFNNKMLLEPRAIKSWAIVNFSF" +
                "PCDSSHISRELISCGMRKGIEIDRPFALVEEDPQYKKAGPVERVEKMIATMKLKFPDPPH" +
                "FILCILPERKTSDIYGPWKKICLTEEGIHTQCICPIKISDQYLTNVLLKINSKLGGINSL" +
                "LGIEYSYNIPLINKIPTLILGMDVSHGPPGRADVPSVAAVVGSKCWPLISRYRAAVRTQS" +
                "PRLEMIDSLFQPIENTEKGDNGIMNELFVEFYRTSRARKPKQIIIFRDGVSESQFEQVLK" +
                "IEVDQIIKAYQRLGESDVPKFTVIVAQKNHHTKLFQAKGPENVPAGTVVDTKIVHPTNYD" +
                "FYMCAHAGKIGTSRPAHYHVLLDEIGFSPDDLQNLIHSLSYVNQRSTTATSIVAPVRYAH" +
                "LAAAQVAQFTKFEGISEDGKVPELPRLHENVEGNMFFC");
        ProteinSequence s2 = new ProteinSequence("MDLLDKVMGEMGSKPGSTAKKPATSASSTPRTNVWGTAKKPSSQQQPPKPLFTTPGSQQG" +
                "SLGGRIPKREHTDRTGPDPKRKPLGGLSVPDSFNNFGTFRVQMNAWNLDISKMDERISRI" +
                "MFRATLVHTDGRRFELSLGVSAFSGDVNRQQRRQAQCLLFRAWFKRNPELFKGMTDPAIA" +
                "AYDAAETIYVGCSFFDVELTEHVCHLTEADFSPQEWKIVSLISRRSGSTFEIRIKTNPPI" +
                "YTRGPNALTLENRSELTRIIEAITDQCLHNEKFLLYSSGTFPTKGGDIASPDEVTLIKSG" +
                "FVKTTKIVDRDGVPDAIMTVDTTKSPFYKDTSLLKFFTAKMDQLTNSGGGPRGHNGGRER" +
                "RDGGGNSRKYDDRRSPRDGEIDYDERTVSHYQRQFQDERISDGMLNTLKQSLKGLDCQPI" +
                "HLKDSKANRSIMIDEIHTGTADSVTFEQKLPDGEMKLTSITEYYLQRYNYRLKFPHLPLV" +
                "TSKRAKCYDFYPMELMSILPGQRIKQSHMTVDIQSYMTGKMSSLPDQHIKQSKLVLTEYL" +
                "KLGDQPANRQMDAFRVSLKSIQPIVTNAHWLSPPDMKFANNQLYSLNPTRGVRFQTNGKF" +
                "VMPARVKSVTIINYDKEFNRNVDMFAEGLAKHCSEQGMKFDSRPNSWKKVNLGSSDRRGT" +
                "KVEIEEAIRNGVTIVFGIIAEKRPDMHDILKYFEEKLGQQTIQISSETADKFMRDHGGKQ" +
                "TIDNVIRKLNPKCGGTNFLIDVPESVGHRVVCNNSAEMRAKLYAKTQFIGFEMSHTGART" +
                "RFDIQKVMFDGDPTVVGVAYSLKHSAQLGGFSYFQESRLHKLTNLQEKMQICLNAYEQSS" +
                "SYLPETVVVYRVGSGEGDYPQIVNEVNEMKLAARKKKHGYNPKFLVICTQRNSHIRVFPE" +
                "HINERGKSMEQNVKSGTCVDVPGASHGYEEFILCCQTPLIGTVKPTKYTIIVNDCRWSKN" +
                "EIMNVTYHLAFAHQVSYAPPAIPNVSYAAQNLAKRGHNNYKTHTKLVDMNDYSYRIKEKH" +
                "EEIISSEEVDDILMRDFIETVSNDLNAMTINGRNFWA");
        SubstitutionMatrix<AminoAcidCompound> matrix = SimpleSubstitutionMatrix.getBlosum62();
        SequencePair<ProteinSequence, AminoAcidCompound> pair = Alignments.getPairwiseAlignment(s1, s2,
                Alignments.PairwiseSequenceAlignerType.LOCAL, new SimpleGapPenalty(), matrix);
        System.out.printf("%n%s vs %s%n%s", pair.getQuery().getAccession(), pair.getTarget().getAccession(), pair);
    }

    private static ProteinSequence getSequenceForId(String uniProtId) throws Exception {
        URL uniprotFasta = new URL(String.format("http://www.uniprot.org/uniprot/%s.fasta", uniProtId));
        ProteinSequence seq = FastaReaderHelper.readFastaProteinSequence(uniprotFasta.openStream()).get(uniProtId);
        System.out.printf("id : %s %s%n%s%n", uniProtId, seq, seq.getOriginalHeader());
        return seq;
    }
}
