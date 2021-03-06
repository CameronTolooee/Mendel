/*
Copyright (c) 2015, Colorado State University
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

package mendel.dht.hash;

import mendel.data.Metadata;
import mendel.util.Checksum;

import java.math.BigInteger;
import java.util.Random;

/**
 * Provides an SHA1 HashFunction.
 *
 * @author malensek
 */
public class SHA1 implements HashFunction<Metadata> {

    private Checksum checksum = new Checksum();
    private Random random = new Random();

    @Override
    public synchronized BigInteger hash(Metadata data) throws HashException {
        return new BigInteger(1, checksum.hash(data.getSegment().toString().getBytes()));
    }

    public synchronized BigInteger hash(long data) throws HashException {
        return new BigInteger(1, checksum.hash(longToBytes(data)));
    }

    public static byte[] longToBytes(long l) {
        int size = Long.SIZE/Byte.SIZE;
        byte[] result = new byte[size];
        for (int i = size - 1; i >= 0; --i) {
            result[i] = (byte)(l & 0xFF);
            l >>= (Byte.SIZE);
        }
        return result;
    }

    @Override
    public BigInteger maxValue() {
        int hashBytes = checksum.getMessageDigest().getDigestLength();
        return BigInteger.valueOf(2).pow(hashBytes * 8);
    }

    @Override
    public synchronized BigInteger randomHash() throws HashException {
        byte[] randomBytes = new byte[1024];
        random.nextBytes(randomBytes);
        return new BigInteger(1, checksum.hash(randomBytes));
    }
}
