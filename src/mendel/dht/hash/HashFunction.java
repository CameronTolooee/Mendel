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

package mendel.dht.hash;

import java.math.BigInteger;

/**
 * Interface for mapping arbitrary data to a specific location in a hash space.
 *
 * @author malensek
 */
public interface HashFunction<T> {

    /**
     * Maps some given data to an integer location in the implemented hash
     * space.
     *
     * @param data Data to hash against.
     *
     * @return location in the hash space for the data.
     */
    public BigInteger hash(T data) throws HashException;

    /**
     * Determines the maximum hash value that this hash function can produce.
     * For example, a 160-bit SHA1 max value would be 2^160.
     *
     * @return maximum possible value this hash function will produce.
     */
    public BigInteger maxValue();

    /**
     * Returns a random location in the hash space.  This is used for seeding
     * the first nodes in an overlay, or providing random positions to place
     * nodes.
     *
     * @return random position in the hash space.
     */
    public BigInteger randomHash() throws HashException;
}
