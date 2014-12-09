/*
 * Copyright (c) 2014, Colorado State University All rights reserved.
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

/**
 *
 *
 * @author ctolooee
 */
package mendel.data;

import mendel.serialize.ByteSerializable;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;

import java.io.IOException;

public class Metadata implements ByteSerializable {

    //TODO Add to this as we figure it out what formats we have for meta
    private String seqBlock;
    private String name;

    public Metadata(String seqBlock, String name) {
        this.seqBlock = seqBlock;
        this.name = name;
    }

    public Metadata(byte[] data, String name) {
        this.seqBlock = new String(data);
        this.name = name;
    }

    public Metadata(SerializationInputStream in) throws IOException {
        this.seqBlock = in.readString();
        this.name = in.readString();
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeString(seqBlock);
        out.writeString(name);
    }

    public String getSeqBlock() {
        return seqBlock;
    }

    public String getName() {
        return name;
    }
}