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

package mendel.comm;

import java.io.IOException;

import mendel.fs.Block;
import mendel.event.Event;
import mendel.serialize.SerializationException;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;

/**
 * Represents an internal storage event at a {@link mendel.dht.StorageNode}
 * to store sequence segments within the storage blocks.
 *
 * @author ctolooee
 */
public class StorageEvent implements Event {

    private Block block;

    /**
     * Constructs a StorageEvent to store the given Block.
     * @param block the block to be stored.
     */
    public StorageEvent(Block block) {
        this.block = block;
    }

    /**
     * Returns the block to be stored.
     * @return the block to be stored
     */
    public Block getBlock() {
        return block;
    }

    @Deserialize
    public StorageEvent(SerializationInputStream in)
            throws IOException, SerializationException {
        block = new Block(in);
    }

    @Override
    public void serialize(SerializationOutputStream out)
            throws IOException {
        block.serialize(out);
    }
}