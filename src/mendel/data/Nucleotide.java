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

package mendel.data;

/**
 * Encapsulates a nucleotide base in a DNA sequence.
 *
 * @author ctolooee
 */
public class Nucleotide {

    /**
     * Encoding Nucleotide characters:
     * A = 00000000
     * C = 00000001
     * G = 00000010
     * N = 00000011
     * T = 00000100
     * <p/>
     * Use this to bitwise OR individual nucleotides into a single byte.
     */
    public static final byte[] charMap = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, (byte) 1,
            0, 0, 0, (byte) 2, 0, 0, 0, 0, 0, 0, (byte) 3, 0, 0, 0, 0, 0,
            (byte) 4};

    public static final char[] reverseCharMap = new char[]{'A', 'C', 'G',
            'N', 'T'};

    public static final char[] reverseComplementCharMap
            = new char[]{'T', 'G', 'C', 'N', 'A'};

}