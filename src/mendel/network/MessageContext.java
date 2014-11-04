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

package mendel.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Contains connection-specific information about the source of a
 * {@link mendel.network.GalileoMessage}.
 *
 * @author malensek
 */
public class MessageContext {

    private MessageRouter router;
    private SelectionKey key;

    public MessageContext(MessageRouter router, SelectionKey key) {
        this.router = router;
        this.key = key;
    }

    /**
     * Retrieves the originating endpoint that sent the message associated with
     * this context.
     */
    public NetworkDestination getNetworkDestination() {
        return MessageRouter.getDestination((SocketChannel) key.channel());
    }

    public MessageRouter getMessageRouter() {
        return router;
    }

    public SelectionKey getSelectionKey() {
        return key;
    }

    public SocketChannel getSocketChannel() {
        return (SocketChannel) key.channel();
    }

    /**
     * @return Server port number that this MessageContext's parent
     * message was sent to.
     */
    public int getServerPort() {
        return getSocketChannel().socket().getLocalPort();
    }

    /**
     * @return NetworkDestination of the client that generated the message.
     */
    public NetworkDestination getSource() {
        return NetworkDestination.fromSocketChannel(getSocketChannel());
    }

    /**
     * Sends a message back to the originator of the message this context
     * belongs to.
     */
    public void sendMessage(GalileoMessage message)
    throws IOException {
        router.sendMessage(this.key, message);
    }
}
