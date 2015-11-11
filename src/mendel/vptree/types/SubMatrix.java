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

package mendel.vptree.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SubMatrix {

    /**
     * An iteration on the Blosum62 substitution matrix with diagonals
     * normalized to 0 to be used as a valid metric to define distances between
     * sequences.
     */
    public static Map<String, Double> SUB_MATRIX = new HashMap<>();

    static {
        SUB_MATRIX.put("CC", 0.0);  SUB_MATRIX.put("CS", 10.0);
        SUB_MATRIX.put("CT", 10.0); SUB_MATRIX.put("CP", 12.0);
        SUB_MATRIX.put("CA", 9.0);  SUB_MATRIX.put("CG", 12.0);
        SUB_MATRIX.put("CN", 12.0); SUB_MATRIX.put("CD", 12.0);
        SUB_MATRIX.put("CE", 13.0); SUB_MATRIX.put("CQ", 12.0);
        SUB_MATRIX.put("CH", 12.0); SUB_MATRIX.put("CR", 12.0);
        SUB_MATRIX.put("CK", 12.0); SUB_MATRIX.put("CM", 10.0);
        SUB_MATRIX.put("CI", 10.0); SUB_MATRIX.put("CL", 10.0);
        SUB_MATRIX.put("CV", 10.0); SUB_MATRIX.put("CF", 11.0);
        SUB_MATRIX.put("CY", 11.0); SUB_MATRIX.put("CW", 11.0);
        SUB_MATRIX.put("SC", 10.0); SUB_MATRIX.put("TC", 10.0);
        SUB_MATRIX.put("PC", 12.0); SUB_MATRIX.put("AC", 9.0);
        SUB_MATRIX.put("GC", 12.0); SUB_MATRIX.put("NC", 12.0);
        SUB_MATRIX.put("DC", 12.0); SUB_MATRIX.put("EC", 13.0);
        SUB_MATRIX.put("QC", 12.0); SUB_MATRIX.put("HC", 12.0);
        SUB_MATRIX.put("RC", 12.0); SUB_MATRIX.put("KC", 12.0);
        SUB_MATRIX.put("MC", 10.0); SUB_MATRIX.put("IC", 10.0);
        SUB_MATRIX.put("LC", 10.0); SUB_MATRIX.put("VC", 10.0);
        SUB_MATRIX.put("FC", 11.0); SUB_MATRIX.put("YC", 11.0);
        SUB_MATRIX.put("WC", 11.0);

        SUB_MATRIX.put("SS", 0.0);  SUB_MATRIX.put("ST", 3.0);
        SUB_MATRIX.put("SP", 5.0);  SUB_MATRIX.put("SA", 3.0);
        SUB_MATRIX.put("SG", 4.0);  SUB_MATRIX.put("SN", 3.0);
        SUB_MATRIX.put("SD", 4.0);  SUB_MATRIX.put("SE", 4.0);
        SUB_MATRIX.put("SQ", 4.0);  SUB_MATRIX.put("SH", 5.0);
        SUB_MATRIX.put("SR", 5.0);  SUB_MATRIX.put("SK", 4.0);
        SUB_MATRIX.put("SM", 5.0);  SUB_MATRIX.put("SI", 6.0);
        SUB_MATRIX.put("SL", 6.0);  SUB_MATRIX.put("SV", 6.0);
        SUB_MATRIX.put("SF", 6.0);  SUB_MATRIX.put("SY", 6.0);
        SUB_MATRIX.put("SW", 7.0);

        SUB_MATRIX.put("TS", 3.0);  SUB_MATRIX.put("PS", 5.0);
        SUB_MATRIX.put("AS", 3.0);  SUB_MATRIX.put("GS", 4.0);
        SUB_MATRIX.put("NS", 3.0);  SUB_MATRIX.put("DS", 4.0);
        SUB_MATRIX.put("ES", 4.0);  SUB_MATRIX.put("QS", 4.0);
        SUB_MATRIX.put("HS", 5.0);  SUB_MATRIX.put("RS", 5.0);
        SUB_MATRIX.put("KS", 4.0);  SUB_MATRIX.put("MS", 5.0);
        SUB_MATRIX.put("IS", 6.0);  SUB_MATRIX.put("LS", 6.0);
        SUB_MATRIX.put("VS", 6.0);  SUB_MATRIX.put("FS", 6.0);
        SUB_MATRIX.put("YS", 6.0);  SUB_MATRIX.put("WS", 7.0);

        SUB_MATRIX.put("TT", 0.0);  SUB_MATRIX.put("TP", 6.0);
        SUB_MATRIX.put("TA", 5.0);  SUB_MATRIX.put("TG", 7.0);
        SUB_MATRIX.put("TN", 5.0);  SUB_MATRIX.put("TD", 6.0);
        SUB_MATRIX.put("TE", 6.0);  SUB_MATRIX.put("TQ", 6.0);
        SUB_MATRIX.put("TH", 7.0);  SUB_MATRIX.put("TR", 6.0);
        SUB_MATRIX.put("TK", 6.0);  SUB_MATRIX.put("TM", 6.0);
        SUB_MATRIX.put("TI", 6.0);  SUB_MATRIX.put("TL", 6.0);
        SUB_MATRIX.put("TV", 5.0);  SUB_MATRIX.put("TF", 7.0);
        SUB_MATRIX.put("TY", 7.0);  SUB_MATRIX.put("TW", 7.0);

        SUB_MATRIX.put("PT", 6.0);  SUB_MATRIX.put("AT", 5.0);
        SUB_MATRIX.put("GT", 7.0);  SUB_MATRIX.put("NT", 5.0);
        SUB_MATRIX.put("DT", 6.0);  SUB_MATRIX.put("ET", 6.0);
        SUB_MATRIX.put("QT", 6.0);  SUB_MATRIX.put("HT", 7.0);
        SUB_MATRIX.put("RT", 6.0);  SUB_MATRIX.put("KT", 6.0);
        SUB_MATRIX.put("MT", 6.0);  SUB_MATRIX.put("IT", 6.0);
        SUB_MATRIX.put("LT", 6.0);  SUB_MATRIX.put("VT", 5.0);
        SUB_MATRIX.put("FT", 7.0);  SUB_MATRIX.put("YT", 7.0);
        SUB_MATRIX.put("WT", 7.0);

        SUB_MATRIX.put("PP", 0.0);  SUB_MATRIX.put("PA", 8.0);
        SUB_MATRIX.put("PG", 9.0);  SUB_MATRIX.put("PN", 9.0);
        SUB_MATRIX.put("PD", 8.0);  SUB_MATRIX.put("PE", 8.0);
        SUB_MATRIX.put("PQ", 8.0);  SUB_MATRIX.put("PH", 9.0);
        SUB_MATRIX.put("PR", 9.0);  SUB_MATRIX.put("PK", 8.0);
        SUB_MATRIX.put("PM", 9.0);  SUB_MATRIX.put("PI", 10.0);
        SUB_MATRIX.put("PL", 10.0); SUB_MATRIX.put("PV", 9.0);
        SUB_MATRIX.put("PF", 11.0); SUB_MATRIX.put("PY", 10.0);
        SUB_MATRIX.put("PW", 11.0);

        SUB_MATRIX.put("AP", 8.0);  SUB_MATRIX.put("GP", 9.0);
        SUB_MATRIX.put("NP", 9.0);  SUB_MATRIX.put("DP", 8.0);
        SUB_MATRIX.put("EP", 8.0);  SUB_MATRIX.put("QP", 8.0);
        SUB_MATRIX.put("HP", 9.0);  SUB_MATRIX.put("RP", 9.0);
        SUB_MATRIX.put("KP", 8.0);  SUB_MATRIX.put("MP", 9.0);
        SUB_MATRIX.put("IP", 10.0); SUB_MATRIX.put("LP", 10.0);
        SUB_MATRIX.put("VP", 9.0);  SUB_MATRIX.put("FP", 11.0);
        SUB_MATRIX.put("YP", 10.0); SUB_MATRIX.put("WP", 11.0);

        SUB_MATRIX.put("AA", 0.0);  SUB_MATRIX.put("AG", 4.0);
        SUB_MATRIX.put("AN", 6.0);  SUB_MATRIX.put("AD", 6.0);
        SUB_MATRIX.put("AE", 3.0);  SUB_MATRIX.put("AQ", 3.0);
        SUB_MATRIX.put("AH", 6.0);  SUB_MATRIX.put("AR", 3.0);
        SUB_MATRIX.put("AK", 3.0);  SUB_MATRIX.put("AM", 3.0);
        SUB_MATRIX.put("AI", 3.0);  SUB_MATRIX.put("AL", 3.0);
        SUB_MATRIX.put("AV", 4.0);  SUB_MATRIX.put("AF", 6.0);
        SUB_MATRIX.put("AY", 6.0);  SUB_MATRIX.put("AW", 7.0);

        SUB_MATRIX.put("GA", 4.0);  SUB_MATRIX.put("NA", 6.0);
        SUB_MATRIX.put("DA", 6.0);  SUB_MATRIX.put("EA", 3.0);
        SUB_MATRIX.put("QA", 3.0);  SUB_MATRIX.put("HA", 6.0);
        SUB_MATRIX.put("RA", 3.0);  SUB_MATRIX.put("KA", 3.0);
        SUB_MATRIX.put("MA", 3.0);  SUB_MATRIX.put("IA", 3.0);
        SUB_MATRIX.put("LA", 3.0);  SUB_MATRIX.put("VA", 4.0);
        SUB_MATRIX.put("FA", 6.0);  SUB_MATRIX.put("YA", 6.0);
        SUB_MATRIX.put("WA", 7.0);

        SUB_MATRIX.put("GG", 0.0);  SUB_MATRIX.put("GN", 6.0);
        SUB_MATRIX.put("GD", 7.0);  SUB_MATRIX.put("GE", 8.0);
        SUB_MATRIX.put("GQ", 8.0);  SUB_MATRIX.put("GH", 8.0);
        SUB_MATRIX.put("GR", 8.0);  SUB_MATRIX.put("GK", 8.0);
        SUB_MATRIX.put("GM", 9.0);  SUB_MATRIX.put("GI", 10.0);
        SUB_MATRIX.put("GL", 10.0); SUB_MATRIX.put("GV", 9.0);
        SUB_MATRIX.put("GF", 9.0);  SUB_MATRIX.put("GY", 9.0);
        SUB_MATRIX.put("GW", 8.0);

        SUB_MATRIX.put("NG", 6.0);  SUB_MATRIX.put("DG", 7.0);
        SUB_MATRIX.put("EG", 8.0);  SUB_MATRIX.put("QG", 8.0);
        SUB_MATRIX.put("HG", 8.0);  SUB_MATRIX.put("RG", 8.0);
        SUB_MATRIX.put("KG", 8.0);  SUB_MATRIX.put("MG", 9.0);
        SUB_MATRIX.put("IG", 10.0); SUB_MATRIX.put("LG", 10.0);
        SUB_MATRIX.put("VG", 9.0);  SUB_MATRIX.put("FG", 9.0);
        SUB_MATRIX.put("YG", 9.0);  SUB_MATRIX.put("WG", 8.0);

        SUB_MATRIX.put("NN", 0.0);  SUB_MATRIX.put("DN", 5.0);
        SUB_MATRIX.put("EN", 6.0);  SUB_MATRIX.put("QN", 6.0);
        SUB_MATRIX.put("HN", 5.0);  SUB_MATRIX.put("RN", 6.0);
        SUB_MATRIX.put("KN", 6.0);  SUB_MATRIX.put("MN", 8.0);
        SUB_MATRIX.put("IN", 9.0);  SUB_MATRIX.put("LN", 9.0);
        SUB_MATRIX.put("VN", 9.0);  SUB_MATRIX.put("FN", 9.0);
        SUB_MATRIX.put("YN", 8.0);  SUB_MATRIX.put("WN", 10.0);

        SUB_MATRIX.put("ND", 5.0);  SUB_MATRIX.put("NE", 6.0);
        SUB_MATRIX.put("NQ", 6.0);  SUB_MATRIX.put("NH", 5.0);
        SUB_MATRIX.put("NR", 6.0);  SUB_MATRIX.put("NK", 6.0);
        SUB_MATRIX.put("NM", 8.0);  SUB_MATRIX.put("NI", 9.0);
        SUB_MATRIX.put("NL", 9.0);  SUB_MATRIX.put("NV", 9.0);
        SUB_MATRIX.put("NF", 9.0);  SUB_MATRIX.put("NY", 8.0);
        SUB_MATRIX.put("NW", 10.0);

        SUB_MATRIX.put("DD", 0.0);  SUB_MATRIX.put("ED", 4.0);
        SUB_MATRIX.put("QD", 6.0);  SUB_MATRIX.put("HD", 7.0);
        SUB_MATRIX.put("RD", 8.0);  SUB_MATRIX.put("KD", 7.0);
        SUB_MATRIX.put("MD", 9.0);  SUB_MATRIX.put("ID", 9.0);
        SUB_MATRIX.put("LD", 10.0); SUB_MATRIX.put("VD", 9.0);
        SUB_MATRIX.put("FD", 9.0);  SUB_MATRIX.put("YD", 9.0);
        SUB_MATRIX.put("WD", 10.0);

        SUB_MATRIX.put("DE", 4.0);  SUB_MATRIX.put("DQ", 6.0);
        SUB_MATRIX.put("DH", 7.0);  SUB_MATRIX.put("DR", 8.0);
        SUB_MATRIX.put("DK", 7.0);  SUB_MATRIX.put("DM", 9.0);
        SUB_MATRIX.put("DI", 9.0);  SUB_MATRIX.put("DL", 10.0);
        SUB_MATRIX.put("DV", 9.0);  SUB_MATRIX.put("DF", 9.0);
        SUB_MATRIX.put("DY", 9.0);  SUB_MATRIX.put("DW", 10.0);

        SUB_MATRIX.put("EE", 0.0);  SUB_MATRIX.put("QE", 3.0);
        SUB_MATRIX.put("HE", 5.0);  SUB_MATRIX.put("RE", 5.0);
        SUB_MATRIX.put("KE", 4.0);  SUB_MATRIX.put("ME", 7.0);
        SUB_MATRIX.put("IE", 8.0);  SUB_MATRIX.put("LE", 8.0);
        SUB_MATRIX.put("VE", 7.0);  SUB_MATRIX.put("FE", 8.0);
        SUB_MATRIX.put("YE", 7.0);  SUB_MATRIX.put("WE", 8.0);

        SUB_MATRIX.put("EQ", 3.0);  SUB_MATRIX.put("EH", 5.0);
        SUB_MATRIX.put("ER", 5.0);  SUB_MATRIX.put("EK", 4.0);
        SUB_MATRIX.put("EM", 7.0);  SUB_MATRIX.put("EI", 8.0);
        SUB_MATRIX.put("EL", 8.0);  SUB_MATRIX.put("EV", 7.0);
        SUB_MATRIX.put("EF", 8.0);  SUB_MATRIX.put("EY", 7.0);
        SUB_MATRIX.put("EW", 8.0);

        SUB_MATRIX.put("QQ", 0.0);  SUB_MATRIX.put("HQ", 5.0);
        SUB_MATRIX.put("RQ", 4.0);  SUB_MATRIX.put("KQ", 4.0);
        SUB_MATRIX.put("MQ", 5.0);  SUB_MATRIX.put("IQ", 8.0);
        SUB_MATRIX.put("LQ", 7.0);  SUB_MATRIX.put("VQ", 7.0);
        SUB_MATRIX.put("FQ", 8.0);  SUB_MATRIX.put("YQ", 6.0);
        SUB_MATRIX.put("WQ", 7.0);

        SUB_MATRIX.put("QH", 5.0);  SUB_MATRIX.put("QR", 4.0);
        SUB_MATRIX.put("QK", 4.0);  SUB_MATRIX.put("QM", 5.0);
        SUB_MATRIX.put("QI", 8.0);  SUB_MATRIX.put("QL", 7.0);
        SUB_MATRIX.put("QV", 7.0);  SUB_MATRIX.put("QF", 8.0);
        SUB_MATRIX.put("QY", 6.0);  SUB_MATRIX.put("QW", 7.0);

        SUB_MATRIX.put("HH", 0.0);  SUB_MATRIX.put("RH", 8.0);
        SUB_MATRIX.put("KH", 9.0);  SUB_MATRIX.put("MH", 10.0);
        SUB_MATRIX.put("IH", 11.0); SUB_MATRIX.put("LH", 11.0);
        SUB_MATRIX.put("VH", 11.0); SUB_MATRIX.put("FH", 9.0);
        SUB_MATRIX.put("YH", 6.0);  SUB_MATRIX.put("WH", 10.0);

        SUB_MATRIX.put("HR", 8.0);  SUB_MATRIX.put("HK", 9.0);
        SUB_MATRIX.put("HM", 10.0); SUB_MATRIX.put("HI", 11.0);
        SUB_MATRIX.put("HL", 11.0); SUB_MATRIX.put("HV", 11.0);
        SUB_MATRIX.put("HF", 9.0);  SUB_MATRIX.put("HY", 6.0);
        SUB_MATRIX.put("HW", 10.0);

        SUB_MATRIX.put("RR", 0.0);  SUB_MATRIX.put("KR", 3.0);
        SUB_MATRIX.put("MR", 6.0);  SUB_MATRIX.put("IR", 8.0);
        SUB_MATRIX.put("LR", 7.0);  SUB_MATRIX.put("VR", 8.0);
        SUB_MATRIX.put("FR", 8.0);  SUB_MATRIX.put("YR", 7.0);
        SUB_MATRIX.put("WR", 8.0);

        SUB_MATRIX.put("RK", 3.0);  SUB_MATRIX.put("RM", 6.0);
        SUB_MATRIX.put("RI", 8.0);  SUB_MATRIX.put("RL", 7.0);
        SUB_MATRIX.put("RV", 8.0);  SUB_MATRIX.put("RF", 8.0);
        SUB_MATRIX.put("RY", 7.0);  SUB_MATRIX.put("RW", 8.0);

        SUB_MATRIX.put("KK", 0.0);  SUB_MATRIX.put("MK", 6.0);
        SUB_MATRIX.put("IK", 8.0);  SUB_MATRIX.put("LK", 7.0);
        SUB_MATRIX.put("VK", 7.0);  SUB_MATRIX.put("FK", 8.0);
        SUB_MATRIX.put("YK", 7.0);  SUB_MATRIX.put("WK", 8.0);

        SUB_MATRIX.put("KM", 6.0);  SUB_MATRIX.put("KI", 8.0);
        SUB_MATRIX.put("KL", 7.0);  SUB_MATRIX.put("KV", 7.0);
        SUB_MATRIX.put("KF", 8.0);  SUB_MATRIX.put("KY", 7.0);
        SUB_MATRIX.put("KW", 8.0);

        SUB_MATRIX.put("MM", 0.0);  SUB_MATRIX.put("IM", 4.0);
        SUB_MATRIX.put("LM", 3.0);  SUB_MATRIX.put("VM", 4.0);
        SUB_MATRIX.put("FM", 5.0);  SUB_MATRIX.put("YM", 6.0);
        SUB_MATRIX.put("WM", 6.0);

        SUB_MATRIX.put("MI", 4.0);  SUB_MATRIX.put("ML", 3.0);
        SUB_MATRIX.put("MV", 4.0);  SUB_MATRIX.put("MF", 5.0);
        SUB_MATRIX.put("MY", 6.0);  SUB_MATRIX.put("MW", 6.0);

        SUB_MATRIX.put("II", 0.0);  SUB_MATRIX.put("LI", 2.0);
        SUB_MATRIX.put("VI", 1.0);  SUB_MATRIX.put("FI", 4.0);
        SUB_MATRIX.put("YI", 5.0);  SUB_MATRIX.put("WI", 7.0);

        SUB_MATRIX.put("IL", 2.0);  SUB_MATRIX.put("IV", 1.0);
        SUB_MATRIX.put("IF", 4.0);  SUB_MATRIX.put("IY", 5.0);
        SUB_MATRIX.put("IW", 7.0);

        SUB_MATRIX.put("LL", 0.0);  SUB_MATRIX.put("VL", 3.0);
        SUB_MATRIX.put("FL", 4.0);  SUB_MATRIX.put("YL", 5.0);
        SUB_MATRIX.put("WL", 6.0);

        SUB_MATRIX.put("LV", 3.0);  SUB_MATRIX.put("LF", 4.0);
        SUB_MATRIX.put("LY", 5.0);  SUB_MATRIX.put("LW", 6.0);

        SUB_MATRIX.put("VV", 0.0);  SUB_MATRIX.put("FV", 5.0);
        SUB_MATRIX.put("YV", 5.0);  SUB_MATRIX.put("WV", 7.0);

        SUB_MATRIX.put("VF", 5.0);  SUB_MATRIX.put("VY", 5.0);
        SUB_MATRIX.put("VW", 7.0);

        SUB_MATRIX.put("FF", 0.0);  SUB_MATRIX.put("YF", 3.0);
        SUB_MATRIX.put("WF", 4.0);

        SUB_MATRIX.put("FY", 3.0);  SUB_MATRIX.put("FW", 4.0);

        SUB_MATRIX.put("YY", 0.0);  SUB_MATRIX.put("WY", 5.0);
        SUB_MATRIX.put("YW", 5.0);

        SUB_MATRIX.put("WW", 0.0);

        /* Make sure no one does modify the substitution values */
        SUB_MATRIX = Collections.unmodifiableMap(SUB_MATRIX);
    }

    public static void main(String[] args) {
        SubMatrix.SUB_MATRIX.entrySet().forEach(System.out::print);
    }
}
