/*
Copyright (c) 2014, Colorado State University
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
package mendel.fs;

import mendel.data.Metadata;
import mendel.serialize.ByteSerializable;
import mendel.serialize.SerializationException;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The basic unit of storage in Mendel.
 *
 * @author ctolooee
 */

public class Block implements ByteSerializable {
    private List<Metadata> metadata;
    private List<byte[]> data;

    public Block(byte[] data, String name) throws IOException, SerializationException {
        this.metadata = new ArrayList<>();
        this.data = new ArrayList<>();
        metadata.add(new Metadata(data, name));
        this.data.add(data);
    }

    public Block(Metadata metadata, byte[] data) {
        this.metadata = new ArrayList<>();
        this.data = new ArrayList<>();
        this.metadata.add(metadata);
        this.data.add(data);
    }

    public Block(List<Metadata> list, List<byte[]> data) {
        this.metadata = list;
        this.data = data;
    }

    public void addData(Metadata meta, byte[] data) {
        this.metadata.add(meta);
        this.data.add(data);
    }


    @Deserialize
    public Block(SerializationInputStream in)
            throws IOException, SerializationException {
        this.metadata = new ArrayList<>();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            metadata.add(new Metadata(in));
        }
        this.data = new ArrayList<>();
        count = in.readInt();
        for (int i = 0; i < count; i++) {
            data.add(in.readField());
        }
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public List<byte[]> getData() {
        return data;
    }

    @Override
    public void serialize(SerializationOutputStream out)
            throws IOException {
        out.writeInt(metadata.size());
        for (Metadata meta : metadata) {
            out.writeSerializable(meta);
        }
        out.writeInt(data.size());
        for (byte[] bytes : data) {
            out.writeField(bytes);
        }
    }
}