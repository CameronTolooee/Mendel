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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles message routing on a {@link java.nio.channels.ServerSocketChannel}.
 * This class is useful for components that must accept incoming requests from
 * clients.
 *
 * @author malensek
 */
public class ServerMessageRouter extends MessageRouter {

    private Thread selectorThread;
    private Map<Integer, ServerSocketChannel> channels = new HashMap<>();

    public ServerMessageRouter() { }

    public ServerMessageRouter(int readBufferSize, int maxWriteQueueSize) {
        super(readBufferSize, maxWriteQueueSize);
    }

    /**
     * Initializes (opens) this MessageRouter's {@link java.nio.channels.Selector} instance.
     */
    private synchronized void initializeSelector()
    throws IOException {
        if (this.selector == null) {
            this.selector = Selector.open();
        }
    }

    /**
     * Starts the selector thread loop and sets this MessageRouter status to
     * online.
     */
    private synchronized void startSelectorThread() {
        if (selectorThread == null || this.online == false) {
            selectorThread = new Thread(this);
            selectorThread.start();
            this.online = true;
        }
    }

    /**
     * Initializes the server socket channel for incoming client connections and
     * begins listening for messages.
     *
     * @param port The port to listen for messages on.
     */
    public void listen(int port)
    throws IOException {
        initializeSelector();

        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(port));
        channel.register(selector, SelectionKey.OP_ACCEPT);
        channels.put(port, channel);

        startSelectorThread();
    }

    /**
     * Closes the server socket channel and stops processing incoming
     * messages.
     */
    public void shutdown() throws IOException {
        for (int port : channels.keySet()) {
            close(port);
        }
        this.online = false;
        selector.wakeup();
    }

    /**
     * @param port Port number to stop listening on.
     */
    public void close(int port) throws IOException {
        ServerSocketChannel channel = channels.get(port);
        if (channel == null) {
            return;
        }

        channel.close();
    }
}
