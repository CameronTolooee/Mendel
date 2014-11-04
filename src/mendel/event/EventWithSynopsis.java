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

package mendel.event;

import java.io.IOException;

import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;

/**
 * Encapsulates a raw (byte[] based) event that includes a String representing
 * the event synopsis.  This can be used to essentially 'tag' particular blobs
 * of data without writing specific events.
 *
 * @author malensek
 */
public class EventWithSynopsis implements Event {

    private String synopsis;
    private byte[] data;
    private boolean compress = false;

    public EventWithSynopsis(String synopsis, byte[] data) {
        this.synopsis = synopsis;
        this.data = data;
    }

    public String getSynopsis() {
        return this.synopsis;
    }

    public byte[] getPayload() {
        return this.data;
    }

    /**
     * Enables compression when serializing this event.  When deserializing,
     * this setting has no effect.
     */
    public void enableCompression() {
        this.compress = true;
    }

    /**
     * Disables compression when serializing this event.  This is the default
     * behavior.  When deserializing, this setting has no effect.
     */
    public void disableCompression() {
        this.compress = false;
    }

    @Deserialize
    public EventWithSynopsis(SerializationInputStream in)
    throws IOException {
        this.synopsis = in.readString();
        this.data = in.readCompressableField();
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        out.writeString(synopsis);
        out.writeCompressableField(data, compress);
    }
}
