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

import mendel.serialize.*;
import mendel.vptree.types.ProteinSequence;

import java.io.IOException;

/**
 * Defines the metadata associated with a genomic segment segment.
 *
 * @author ctolooee
 */
public class Metadata implements ByteSerializable {

    private String name;
    private ProteinSequence segment;

    /**
     * Constructs a Metadata item for the specified sequence segment.
     * @param segment the sequence segment
     * @param name the name of the sequence from which the segment originated
     */
    public Metadata(ProteinSequence segment, String name) {
        this.segment = segment;
        this.name = name;
    }

    @Deserialize
    public Metadata(SerializationInputStream in) throws IOException {
        this.segment = new ProteinSequence(in);
        this.name = in.readString();
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeSerializable(segment);
        out.writeString(name);
    }

    /**
     * Returns the sequence segment.
     * @return the sequence segment
     */
    public ProteinSequence getSegment() {
        return segment;
    }

    /**
     * Returns the name of the sequence from which the segment originates.
     * @return the sequence segments original ID
     */
    public String getName() {
        return name;
    }
}
