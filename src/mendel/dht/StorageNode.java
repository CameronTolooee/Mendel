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

import mendel.comm.QueryEvent;
import mendel.comm.QueryRequest;
import mendel.comm.QueryResponse;
import mendel.config.NetworkConfig;
import mendel.config.SystemConfig;
import mendel.dht.hash.HashException;
import mendel.dht.hash.HashTopologyException;
import mendel.dht.partition.PartitionerException;
import mendel.dht.partition.SHA1Partitioner;
import mendel.event.*;
import mendel.fs.Block;
import mendel.fs.FileSystemException;
import mendel.fs.MendelFileSystem;
import mendel.network.*;
import mendel.serialize.SerializationException;
import mendel.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StorageNode implements Node {
    private static final Logger logger = Logger.getLogger("mendel");

    private NetworkInfo network;

    private ServerMessageRouter messageRouter;

    private int port;
    private String sessionId;
    private String rootDir;
    private SHA1Partitioner partitioner;
    private File pidFile;

    private ClientConnectionPool connectionPool;
    private EventMap eventMap = new EventMap();
    private EventReactor eventReactor = new EventReactor(this, eventMap);
    private MendelFileSystem fileSystem;
    private ConcurrentHashMap<String, QueryTracker> queryTrackers
            = new ConcurrentHashMap<>();

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

    /**
     * Begins Server execution. This method attempts to fail fast to provide
     * immediate feedback to wrapper scripts or other user interface tools. Only
     * once all the prerequisite components are initialized and in a sane state
     * will the StorageNode begin accepting connections.
     */
    @Override
    public void init() throws IOException, EventException,
            InterruptedException, SerializationException,
            HashException, HashTopologyException,
            PartitionerException, FileSystemException {
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

        /* Setup our Shutdown hook */
        Runtime.getRuntime().addShutdownHook(new ShutdownHandler());

        /* Setup file system */
        fileSystem = new MendelFileSystem(SystemConfig.getRootDir());

        /* Pre-scheduler setup tasks */
        connectionPool = new ClientConnectionPool();
        connectionPool.addListener(eventReactor);
        partitioner = new SHA1Partitioner(this, network);

        /* Start listening for incoming messages. */
        messageRouter = new ServerMessageRouter();
        messageRouter.addListener(eventReactor);
        messageRouter.listen(port);

        System.out.println("Listening... ");

        /* Start processing the message loop */
        while (true) {
            eventReactor.processNextEvent();
        }
    }

    private class ShutdownHandler extends Thread {

        @Override
        public void run() {
            /* The logging subsystem may have already shut down, so we revert to
             * stdout. */
            System.out.println("Shutdown initiated");

            try {
                connectionPool.forceShutdown();
                messageRouter.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pidFile != null && pidFile.exists()) {
                pidFile.delete();
            }

            System.out.println("Goodbye!");
        }
    }

    /**
     * Handles a query request from a client.  Query requests result in a number
     * of subqueries being performed across the Mendel network.
     */
    @EventHandler
    public void handleQueryRequest(QueryRequest request, EventContext context)
            throws IOException, SerializationException {

        String queryString = request.getQueryString();
        logger.log(Level.INFO, "Query request: {0}", queryString);

        /* Add query to tracker */
        QueryTracker tracker = new QueryTracker(context);
        String queryID = tracker.getIdString(sessionId);
        queryTrackers.put(queryID, tracker);

        /* Determine StorageNodes that contain relevant data. */
        List<NodeInfo> queryNodes = new ArrayList<>();

        /* TODO: For now just forward to all the nodes */
        queryNodes.addAll(network.getAllNodes());

        QueryEvent query = new QueryEvent(request.getQuery(), queryID);
        for (NodeInfo node : queryNodes) {
            sendEvent(node, query);
        }
    }

    @EventHandler
    public void handleQuery(QueryRequest request, EventContext context)
            throws IOException, SerializationException {

        logger.log(Level.INFO, "Handling query {0}", request.getQueryString());

        Block response = fileSystem.query(request.getQuery());
        List<Block> list = new ArrayList<>();
        list.add(response);
        QueryResponse queryResponse = new QueryResponse(list, request.getQueryID());
        context.sendReply(queryResponse);
    }

    @EventHandler
    public void handleQueryResponse(
            QueryResponse response, EventContext context) throws IOException {

        logger.log(Level.INFO, "Query response to query ID: {0}",
                response.getQueryID());

        /* Get query destination from tracker */
        QueryTracker tracker = queryTrackers.get(response.getQueryID());

        /* Forward the response to the client */
        tracker.getContext().sendReply(response);
    }

    private void sendEvent(NodeInfo node, Event event)
            throws IOException {
        connectionPool.sendMessage(node, eventReactor.wrapEvent(event));
    }
}
