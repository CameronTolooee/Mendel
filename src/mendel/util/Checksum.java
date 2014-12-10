/*
Copyright (c) 2013, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package mendel.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides convenience functions for dealing with MessageDigest algorithms.
 *
 * @author malensek
 */
public class Checksum {

    private static final Logger logger = Logger.getLogger("mendel");

    private MessageDigest md;

    /**
     * Initializes a new Checksum generator using the default SHA-1 algorithm.
     */
    public Checksum() {
        /* We assume the "SHA1" algorithm is always available */
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE,
                    "SHA1 message digest algorithm not found!", e);
        }
    }

    /**
     * Initializes a new Checksum generator using the specified algorithm.
     *
     * @param algorithm algorithm to use to generate checksums.
     */
    public Checksum(String algorithm)
    throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance(algorithm);
    }

    /**
     * Produce a checksum/hashsum of a given block of data.
     *
     * @param bytes data bytes to checksum.
     *
     * @return checksum as a byte array.
     */
    public byte[] hash(byte[] bytes) {
        return md.digest(bytes);
    }

    /**
     * Convert a hash to a hexidecimal String.
     *
     * @param hash the hash value to convert
     *
     * @return zero-padded hex String representation of the hash.
     */
    public String hashToHexString(byte[] hash) {
        BigInteger bigInt = new BigInteger(1, hash);

        /* Determine the max number of hex characters the digest will produce */
        long targetLen = md.getDigestLength() * 2;

        /* Return a formatted zero-padded String */
        return String.format("%0" + targetLen + "x", bigInt);
    }

    /**
     * Retrieves the MessageDigest instance used by this Checksum generator.
     *
     * @return the Checksum instance MessageDigest.
     */
    public MessageDigest getMessageDigest() {
        return md;
    }
}
