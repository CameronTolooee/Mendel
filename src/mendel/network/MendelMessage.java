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

package mendel.network;

import java.nio.channels.SelectionKey;

/**
 * The unit of data transmission in the Mendel DHT.  These packets are simple
 * in structure, containing a size prefix followed by the packet payload.
 *
 * @author malensek
 */
public class MendelMessage {

    private byte[] payload;

    private MessageContext context;
    private SelectionKey key;

    /**
     * Constructs a MendelMessage from an array of bytes.
     *
     * @param payload message payload in the form of a byte array.
     */
    public MendelMessage(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Constructs a MendelMessage from an array of bytes with an associated
     * {@link java.nio.channels.SelectionKey} of the message source.
     *
     * @param payload message payload in the form of a byte array.
     * @param key SelectionKey of the message source.
     */
    @Deprecated
    public MendelMessage(byte[] payload, SelectionKey key) {
        this(payload);
        this.key = key;
    }

    /**
     * Constructs a MendelMessage from an array of bytes with an associated
     * {@link MessageContext} representing the source of the message.
     *
     * @param payload message payload in the form of a byte array.
     * @param context context information for this message
     */
    public MendelMessage(byte[] payload, MessageContext context) {
        this(payload);
        this.context = context;
        this.key = context.getSelectionKey();
    }

    /**
     * Retrieves the payload for this MendelMessage.
     *
     * @return the MendelMessage payload
     */
    public byte[] getPayload() {
        return payload;
    }

    public MessageContext getContext() {
        return context;
    }

    @Deprecated
    public SelectionKey getSelectionKey() {
        return key;
    }
}
