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

package mendel.dht;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mendel.config.NetworkConfig;
import mendel.config.SystemConfig;
import mendel.event.EventException;
import mendel.event.EventMap;
import mendel.event.EventReactor;
import mendel.network.ClientConnectionPool;
import mendel.network.HostIdentifier;
import mendel.network.NetworkInfo;
import mendel.network.ServerMessageRouter;
import mendel.serialize.SerializationException;
import mendel.util.Version;

public class StorageNode implements Node {
    private static final Logger logger = Logger.getLogger("mendel");

    private NetworkInfo network;

    private ServerMessageRouter messageRouter;

    private int port;
    private String sessionId;
    private String rootDir;
    private File pidFile;

    private ClientConnectionPool connectionPool;
    private EventMap eventMap = new EventMap();
    private EventReactor eventReactor = new EventReactor(this, eventMap);
    public StorageNode() {
        this.port = NetworkConfig.DEFAULT_PORT;
        this.rootDir = SystemConfig.getRootDir();
        this.sessionId = HostIdentifier.getSessionID(port);
        String pid = System.getProperty("pidFile");
        if (pid != null) {
            this.pidFile = new File(pid);
        }
    }

    /**
     * Begins Server execution. This method attempts to fail fast to provide
     * immediate feedback to wrapper scripts or other user interface tools. Only
     * once all the prerequisite components are initialized and in a sane state
     * will the StorageNode begin accepting connections.
     */
    @Override
    public void init() throws IOException, EventException, InterruptedException, SerializationException {
        Version.printSplash();

        /*
         * Read the network configuration; if this is invalid, there is no need
         * to execute the rest of this method.
         */
        try {
            network = NetworkConfig.readNetworkDescription(SystemConfig
                    .getConfDir());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not setup network infrastructure!",
                    e);
            return;
        } 
        System.out.println(network);
        /* Pre-scheduler setup tasks */
        connectionPool = new ClientConnectionPool();
        connectionPool.addListener(eventReactor);

        /* Start listening for incoming messages. */
        messageRouter = new ServerMessageRouter();
        messageRouter.addListener(eventReactor);
        messageRouter.listen(port);

        /* Start processing the message loop */
        while (true) {
            eventReactor.processNextEvent();
        }
    }

    /**
     * Executable to be run on each Mendel storage server.
     * 
     * @param args No args
     */
    public static void main(String[] args) {
        Node node = new StorageNode();
        try {
            node.init();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "StorageNode failed to start.", e);
        }
    }
}
