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

import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an abstract implementation for consuming and publishing messages on
 * both the server and client side.
 *
 * @author malensek
 */
public abstract class MessageRouter implements Runnable {

    protected static final Logger logger = Logger.getLogger("mendel");

    /**
     * The size (in bytes) of the message prefix used in the system.
     */
    public static final int PREFIX_SZ = Integer.SIZE / Byte.SIZE;

    /**
     * The default read buffer size is 8 MB.
     */
    public static final int DEFAULT_READ_BUFFER_SIZE = 8388608;

    /**
     * The default write queue allows 100 items to be inserted before it
     * starts blocking.  This prevents situations where the MessageRouter is
     * overwhelmed by an extreme number of write requests, exhausting available
     * resources.
     */
    public static final int DEFAULT_WRITE_QUEUE_SIZE = 100;

    /**
     * System property that overrides the read buffer size.
     */
    public static final String READ_BUFFER_PROPERTY
            = "mendel.network.MessageRouter.readBufferSize";

    /**
     * System property that overrides the write queue maximum size.
     */
    public static final String WRITE_QUEUE_PROPERTY
            = "mendel.network.MessageRouter.writeQueueSize";

    /**
     * Flag used to determine whether the Selector thread should run
     */
    protected boolean online;

    private List<MessageListener> listeners = new ArrayList<>();

    protected Selector selector;

    protected int readBufferSize;
    protected int writeQueueSize;
    private ByteBuffer readBuffer;

    protected ConcurrentHashMap<SelectionKey, Integer> changeInterest
            = new ConcurrentHashMap<>();

    public MessageRouter() {
        this(DEFAULT_READ_BUFFER_SIZE, DEFAULT_WRITE_QUEUE_SIZE);
    }

    public MessageRouter(int readBufferSize, int maxWriteQueueSize) {
        String readSz = System.getProperty(READ_BUFFER_PROPERTY);
        if (readSz == null) {
            this.readBufferSize = readBufferSize;
        } else {
            this.readBufferSize = Integer.parseInt(readSz);
        }

        String queueSz = System.getProperty(WRITE_QUEUE_PROPERTY);
        if (queueSz == null) {
            this.writeQueueSize = maxWriteQueueSize;
        } else {
            this.writeQueueSize = Integer.parseInt(queueSz);
        }

        readBuffer = ByteBuffer.allocateDirect(this.readBufferSize);
    }

    /**
     * As long as the MessageRouter is online, monitor connection operations
     * through the Selector instance.
     */
    @Override
    public void run() {
        while (online) {
            try {
                updateInterestOps();
                processSelectionKeys();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error in selector thread", e);
            }
        }
    }

    /**
     * Updates interest sets for any SelectionKey instances that require
     * changes.  This allows external threads to queue up changes to the
     * interest sets that will be fulfilled by the selector thread.
     */
    protected void updateInterestOps() {
        Iterator<SelectionKey> it = changeInterest.keySet().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            if (key.isValid()) {
                SocketChannel channel = (SocketChannel) key.channel();
                if (channel.isConnected() == false
                        || channel.isRegistered() == false) {
                    continue;
                }
                key.interestOps(changeInterest.get(key));
            }
            changeInterest.remove(key);
        }
    }

    /**
     * Performs a select operation, and then processes the resulting
     * SelectionKey set based on interest ops.
     */
    protected void processSelectionKeys()
            throws IOException {

        selector.select();

        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();

            if (key.isValid() == false) {
                continue;
            }

            try {

                if (key.isAcceptable()) {
                    accept(key);
                    continue;
                }

                if (key.isConnectable()) {
                    connect(key);
                    continue;
                }

                if (key.isWritable()) {
                    write(key);
                }

                if (key.isReadable()) {
                    read(key);
                }

            } catch (CancelledKeyException e) {
                /* SelectionKey was cancelled by another thread. */
                continue;
            }
        }
    }

    /**
     * Accepts new connections.
     *
     * @param key The SelectionKey for the connecting client.
     */
    protected void accept(SelectionKey key)
            throws IOException {
        ServerSocketChannel servSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = servSocket.accept();
        logger.info("Accepted connection: " + getClientString(channel));

        TransmissionTracker tracker = new TransmissionTracker(writeQueueSize);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, tracker);

        dispatchConnect(getDestination(channel));
    }

    /**
     * Finishes setting up a connection on a SocketChannel.
     *
     * @param key SelectionKey for the SocketChannel.
     */
    protected void connect(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();

            if (channel.finishConnect()) {
                TransmissionTracker tracker = TransmissionTracker.fromKey(key);
                if (tracker.hasPendingData() == false) {
                    changeInterest.put(key, SelectionKey.OP_READ);
                } else {
                    /* Data has already been queued up; start writing */
                    changeInterest.put(key,
                            SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }
            }

            dispatchConnect(getDestination(channel));
        } catch (IOException e) {
            logger.log(Level.INFO, "Connection finalization failed", e);
            disconnect(key);
        }
    }

    /**
     * Read data from a SocketChannel.
     *
     * @param key SelectionKey for the SocketChannel.
     */
    protected void read(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        readBuffer.clear();

        int bytesRead = 0;

        try {
            /* Read data from the channel */
            while ((bytesRead = channel.read(readBuffer)) > 0) {
                readBuffer.flip();
                processIncomingMessage(key);
            }
        } catch (IOException e) {
            logger.log(Level.FINE, "Abnormal remote termination", e);
            disconnect(key);
            return;
        } catch (BufferUnderflowException e) {
            /* Incoming packets lied about their size! */
            logger.log(Level.WARNING, "Incoming packet size mismatch", e);
        }

        if (bytesRead == -1) {
            /* Connection was terminated by the client. */
            logger.fine("Reached EOF in channel input stream");
            disconnect(key);
            return;
        }
    }

    /**
     * Process data received from a client SocketChannel.  This method is
     * chiefly concerned with processing incoming data streams into
     * MendelMessage packets to be consumed by the system.
     *
     * @param key SelectionKey for the client.
     */
    protected void processIncomingMessage(SelectionKey key) {
        TransmissionTracker transmission = TransmissionTracker.fromKey(key);
        if (transmission.expectedBytes == 0) {
            /* We don't know how much data the client is sending yet.
             * Read the message prefix to determine the payload size. */
            boolean ready = readPrefix(readBuffer, transmission);

            /* Check if we have read the payload size prefix yet.  If
             * not, then we're done for now. */
            if (ready == false) {
                return;
            }
        }

        int readSize = transmission.expectedBytes - transmission.readPointer;
        if (readSize > readBuffer.remaining()) {
            readSize = readBuffer.remaining();
        }

        readBuffer.get(transmission.payload,
                transmission.readPointer, readSize);
        transmission.readPointer += readSize;

        if (transmission.readPointer == transmission.expectedBytes) {
            /* The payload has been read */
            MendelMessage msg = new MendelMessage(
                    transmission.payload,
                    new MessageContext(this, key));
            dispatchMessage(msg);
            transmission.resetCounters();

            if (readBuffer.hasRemaining()) {
                /* There is another payload to read */
                processIncomingMessage(key);
                /* Note: this process continues until we reach the end of the
                 * buffer.  Not doing so would cause us to lose data. */
            }
        }
    }

    /**
     * Read the payload size prefix from a channel.
     * Each message in Mendel is prefixed with a payload size field; this is
     * read to allocate buffers for the incoming message.
     *
     * @return true if the payload size has been determined; false otherwise.
     */
    protected static boolean readPrefix(ByteBuffer buffer,
                                        TransmissionTracker transmission) {
        /* Make sure the prefix hasn't already been read. */
        if (transmission.expectedBytes != 0) {
            return true;
        }

        /* Can we determine the payload size in one shot? (we must read at least
         * PREFIX_SZ bytes) */
        if (transmission.prefixPointer == 0
                && buffer.remaining() >= PREFIX_SZ) {
            transmission.expectedBytes = buffer.getInt();
            transmission.allocatePayload();
            return true;
        } else {
            /* Keep reading until we have at least PREFIX_SZ bytes to determine
             * the payload size.  */

            int prefixLeft = PREFIX_SZ - transmission.prefixPointer;
            if (buffer.remaining() < prefixLeft) {
                prefixLeft = buffer.remaining();
            }

            buffer.get(transmission.prefix,
                    transmission.prefixPointer, prefixLeft);
            transmission.prefixPointer += prefixLeft;

            if (transmission.prefixPointer >= PREFIX_SZ) {
                ByteBuffer buf = ByteBuffer.wrap(transmission.prefix);
                transmission.expectedBytes = buf.getInt();
                transmission.allocatePayload();
                return true;
            }
        }

        return false;
    }

    /**
     * Wraps a given message in a {@link java.nio.ByteBuffer}, including the payload size
     * prefix.  Data produced by this method will be subsequently read by the
     * readPrefix() method.
     */
    protected static ByteBuffer wrapWithPrefix(MendelMessage message) {
        int messageSize = message.getPayload().length;
        ByteBuffer buffer = ByteBuffer.allocate(messageSize + 4);
        buffer.putInt(messageSize);
        buffer.put(message.getPayload());
        buffer.flip();
        return buffer;
    }

    /**
     * Adds a message to the pending write queue for a particular SelectionKey
     * and submits a change request for its interest set. Pending data is placed
     * in a blocking queue, so this function may block to prevent queueing an
     * excessive amount of data.
     * <p/>
     * The system property <em>mendel.net.MessageRouter.writeQueueSize</em>
     * tunes the maximum amount of data that can be queued.
     *
     * @param key     SelectionKey for the channel.
     * @param message MendelMessage to publish on the channel.
     * @return {@link mendel.network.Transmission} instance representing the send operation.
     */
    public Transmission sendMessage(SelectionKey key, MendelMessage message)
            throws IOException {
        //TODO reduce the visibility of this method to protected
        if (this.isOnline() == false) {
            throw new IOException("MessageRouter is not online.");
        }

        TransmissionTracker tracker = TransmissionTracker.fromKey(key);
        ByteBuffer payload = wrapWithPrefix(message);

        Transmission trans = null;
        try {
            tracker.queueOutgoingData(payload);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting to queue data");
        }

        changeInterest.put(key, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        selector.wakeup();
        return trans;
    }

    /**
     * When a {@link java.nio.channels.SelectionKey} is writable, push as much pending data
     * out on the channel as possible.
     *
     * @param key {@link java.nio.channels.SelectionKey} of the channel to write to.
     */
    private void write(SelectionKey key) {
        TransmissionTracker tracker = TransmissionTracker.fromKey(key);
        SocketChannel channel = (SocketChannel) key.channel();

        while (tracker.hasPendingData() == true) {
            Transmission trans = tracker.getNextTransmission();
            ByteBuffer buffer = trans.getPayload();
            if (buffer == null) {
                break;
            }

            int written = 0;
            while (buffer.hasRemaining()) {
                try {
                    written = channel.write(buffer);
                } catch (IOException e) {
                    /* Broken pipe */
                    disconnect(key);
                    return;
                }

                if (buffer.hasRemaining() == false) {
                    /* Done writing */
                    tracker.transmissionFinished();
                }

                if (written == 0) {
                    /* Return now, to keep our OP_WRITE interest op set. */
                    return;
                }
            }
        }

        /* At this point, the queue is empty. */
        key.interestOps(SelectionKey.OP_READ);
        return;
    }

    /**
     * Handle termination of connections.
     *
     * @param key The SelectionKey of the SocketChannel that has disconnected.
     */
    protected void disconnect(SelectionKey key) {
        if (key.isValid() == false) {
            return;
        }

        SocketChannel channel = (SocketChannel) key.channel();
        NetworkDestination destination = getDestination(channel);
        logger.info("Terminating connection: " + destination.toString());

        try {
            key.cancel();
            key.channel().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to disconnect channel", e);
        }

        dispatchDisconnect(destination);
    }

    /**
     * Adds a message listener (consumer) to this MessageRouter.  Listeners
     * receive messages that are published by this MessageRouter.
     *
     * @param listener {@link mendel.network.MessageListener} that will consume messages
     *                 published by this MessageRouter.
     */
    public void addListener(MessageListener listener) {
        listeners.add(listener);
    }

    /**
     * Dispatches a message to all listening consumers.
     *
     * @param message {@link mendel.network.MendelMessage} to dispatch.
     */
    protected void dispatchMessage(MendelMessage message) {
        for (MessageListener listener : listeners) {
            listener.onMessage(message);
        }
    }

    /**
     * Informs all listening consumers that a connection to a remote endpoint
     * has been made.
     */
    protected void dispatchConnect(NetworkDestination endpoint) {
        for (MessageListener listener : listeners) {
            listener.onConnect(endpoint);
        }
    }

    /**
     * Informs all listening consumers that a connection to a remote endpoint
     * has been terminated.
     */
    protected void dispatchDisconnect(NetworkDestination endpoint) {
        for (MessageListener listener : listeners) {
            listener.onDisconnect(endpoint);
        }
    }

    /**
     * Determines whether or not this MessageRouter is online.  As long as the
     * router is online, the selector thread will continue to run.
     *
     * @return true if the MessageRouter instance is online and running.
     */
    public boolean isOnline() {
        return this.online;
    }

    /**
     * Determines a connection's hostname and port, then concatenates the two
     * values, separated by a colon (:).
     *
     * @param channel Channel to get client information about.
     */
    protected static String getClientString(SocketChannel channel) {
        Socket socket = channel.socket();
        return socket.getInetAddress().getHostName() + ":" + socket.getPort();
    }

    /**
     * Determines a connection's endpoint information (hostname and port) and
     * encapsulates them in a {@link mendel.network.NetworkDestination}.
     *
     * @param channel The SocketChannel of the network endpoint.
     * @return NetworkDestination representation of the endpoint.
     */
    protected static NetworkDestination getDestination(SocketChannel channel) {
        Socket socket = channel.socket();
        return new NetworkDestination(
                socket.getInetAddress().getHostName(),
                socket.getPort());
    }
}
