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

import mendel.comm.*;
import mendel.config.NetworkConfig;
import mendel.config.SystemConfig;
import mendel.data.Metadata;
import mendel.dht.hash.HashException;
import mendel.dht.hash.HashTopologyException;
import mendel.dht.partition.PartitionException;
import mendel.dht.partition.PartitionerException;
import mendel.dht.partition.VPHashPartitioner;
import mendel.event.*;
import mendel.fs.Block;
import mendel.fs.FileSystemException;
import mendel.fs.MendelFileSystem;
import mendel.network.*;
import mendel.query.SimilarityQuery;
import mendel.query.QueryResult;
import mendel.serialize.SerializationException;
import mendel.util.Version;
import mendel.vptree.types.ProteinSequence;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
    private int windowSize;
    private VPHashPartitioner partitioner;
    private File pidFile;
    private long timer;

    private ClientConnectionPool connectionPool;
    private MendelEventMap eventMap = new MendelEventMap();
    private EventReactor eventReactor = new EventReactor(this, eventMap);
    private MendelFileSystem fileSystem;
    private ConcurrentHashMap<String, QueryTracker> queryTrackers
            = new ConcurrentHashMap<>();

    public StorageNode() {
        this.port = NetworkConfig.DEFAULT_PORT;
        this.rootDir = SystemConfig.getRootDir();
        this.windowSize = SystemConfig.getWindowSize();
        this.sessionId = HostIdentifier.getSessionID(port);
        String pid = System.getProperty("pidFile");
        if (pid != null) {
            this.pidFile = new File(pid);
        }
    }

    public StorageNode(boolean debug) {
        System.out.println("Debugging and testing: " + debug);
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
        } catch (PartitionException e) {
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
            PartitionerException, FileSystemException, PartitionException {
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
        boolean psuedoFSMode = SystemConfig.getPseudoFS();
        fileSystem = new MendelFileSystem(
                SystemConfig.getRootDir(), psuedoFSMode);

        /* Pre-scheduler setup tasks */
        connectionPool = new ClientConnectionPool();
        connectionPool.addListener(eventReactor);
        partitioner = new VPHashPartitioner(this, network);

        /* Stage data for the vantage point hashing tree */
        try {
            partitioner.stageData();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to stage data into the vp hash" +
                    "tree", e);
            return;
        }
        /* Start listening for incoming messages. */
        messageRouter = new ServerMessageRouter();
        messageRouter.addListener(eventReactor);
        messageRouter.listen(port);


        System.out.println("Listening... ");

        /* Start processing the message loop */
        while (true) {
        /* TODO: known bug: all event errors cause "SEVERE StorageNode failed to
         * TODO:       to start" message in log; move reactor loop out of init
         */
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

            try {
                fileSystem.shutdown();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Unable to flush index to disk", e);
            }
            System.out.println("Goodbye!");
            // System.out.println(partitioner.getMetadataTreeDOT());
        }
    }

    /**
     * Handles a query request from a client.  Query requests result in a number
     * of subqueries being performed across the Mendel network.
     */
    @EventHandler
    public void handleQueryRequest(QueryRequest request, EventContext context)
            throws IOException, SerializationException, PartitionException, HashException {
        timer = System.currentTimeMillis();
        String queryString = request.getQuery().getQuerySequence();

        /* Add query to tracker */
        QueryTracker tracker = new QueryTracker(context);
        String queryID = tracker.getIdString(sessionId);
        logger.log(Level.INFO, "Query request: {0}", queryID);
        queryTrackers.put(queryID, tracker);

        /* Determine StorageNodes that contain relevant data. */
        List<NodeInfo> queryNodes = new ArrayList<>();
        List<String> subsequences = new ArrayList<>();
        long distributionTime = System.nanoTime();
        int jump = windowSize / 2;
        for (int i = 0; i < (queryString.length() - windowSize); i = i + jump) {
            String subsequence = queryString.substring(i, i + windowSize);
            subsequences.add(subsequence);
        }

        //for (String seq : subsequences) {
        //     NodeInfo node = partitioner.locateData(new Metadata(new Sequence(seq),
        //             request.getQueryID()));

        for (NodeInfo node : network.getAllNodes()) {
            sendEvent(node, new QueryEvent(new SimilarityQuery(subsequences,
                    queryString), queryID));
            tracker.incrementSendRecvCount();
        }

        //  }
        distributionTime = System.nanoTime() - distributionTime;
        System.out.printf("Distribution Time: %f\n",
                distributionTime / 1000000000.0);
    }

    /**
     * Performs the query versus the data on this Node and replies the results
     * back to the sender.
     */
    @EventHandler
    public void handleQuery(QueryEvent request, EventContext context)
            throws IOException, SerializationException {
        List<QueryResult> queryResults = new ArrayList<>();
        long start = System.nanoTime();
        for (String subsequence : request.getQuery().getSequenceSegments()) {

            long NNTime = System.nanoTime();

            List<ProteinSequence> resultsNN = fileSystem.nearestNeighboQuery(
                    subsequence);

            NNTime = System.nanoTime() - NNTime;
            System.out.printf("NN Time %f\n",
                    NNTime / 1000000000.0);

            /* filter out low scoring results */
            queryResults.addAll(evaluateNNResults(resultsNN, subsequence));

            NNTime = System.nanoTime() - NNTime;
            System.out.printf("Filter Time %f\n",
                    NNTime / 1000000000.0);


//            resultsNN.forEach(result -> {
//                ProteinSequence query = new ProteinSequence(
//                        request.getQuery().getQuerySequence());
//
//                ProteinSequence value = new ProteinSequence(result);
//                queryResults.add(new QueryResult(query, value));
//            });

        }
        if (queryResults.size() > 0) {
            logger.log(Level.INFO, "Handling query {0}",
                    request.getQueryID());
            QueryResponse queryResponse = new QueryResponse(queryResults,
                    request.getQueryID(),
                    fileSystem.countBlocks(),
                    request.getQuery().getQuerySequence());
            context.sendReply(queryResponse);
        } else {
            /* Respond saying we found nothing */
            QueryResponse queryResponse = new QueryResponse(
                    new ArrayList<QueryResult>(),
                    request.getQueryID(),
                    fileSystem.countBlocks(),
                    request.getQuery().getQuerySequence());

            context.sendReply(queryResponse);
            logger.log(Level.INFO, "Query response is null");
        }
        start = System.nanoTime() - start;
        System.out.printf("Total Node Time %f\n",
                start / 1000000000.0);
    }

    private List<QueryResult> evaluateNNResults(List<ProteinSequence> resultsNN,
                                                String query) {
        List<QueryResult> queryResults = new ArrayList<>();
        for (ProteinSequence sequence : resultsNN) {
            ProteinSequence querySeq = new ProteinSequence(query);
            double distance = sequence.getDistanceTo(querySeq);
            int maxDistance = sequence.getWord().length() * 5;
            if (distance < maxDistance) {
                QueryResult result = new QueryResult(querySeq, sequence);
                queryResults.add(result);
            }

        }
        return queryResults;
    }

    /**
     * Forwards all the queries responses from the initial query request back to
     * the client.
     */
    @EventHandler
    public void handleQueryResponse(
            QueryResponse response, EventContext context) throws IOException {

        logger.log(Level.INFO, "Query response to query ID: {0}",
                response.getQueryID());

        /* Get query destination from tracker */
        QueryTracker tracker = queryTrackers.get(response.getQueryID());
        System.out.println("Got response #" + tracker.getSendRecvCount()
                + " from " + context.getSource());
        /* Forward the response to the client after all results are received */
        tracker.decrementSendRecvCount();
        /* TODO Add a timeout to for query results */
        tracker.addResults(response.getResponse());
        if (tracker.getSendRecvCount() == 0) {
            System.out.printf("Total NN timer: %f s\n", timer / 1000000000.0);
            List<QueryResult> finalEvaluation = evaluateFinalResults(tracker);
            tracker.getContext().sendReply(new QueryResponse(finalEvaluation,
                    response.getQueryID(), 1, response.getQuery()));
        }
    }

    private List<QueryResult> evaluateFinalResults(QueryTracker tracker) {
        timer = System.nanoTime();
        HashMap<String, List<QueryResult>> map = new HashMap<>();
        List<QueryResult> resultList = new ArrayList<>();
        System.out.println("Got " + tracker.getResults().size());
        System.out.println("------ FINDING HSM'S ------");
        for (QueryResult result : tracker.getResults()) {
                /* High scoring match --> hash */
            String matchID = result.getValue().getSequenceID();
            List<QueryResult> tmp = map.get(matchID);
            if (tmp != null) {
                tmp.add(result);
            } else {
                ArrayList<QueryResult> list = new ArrayList<>();
                list.add(result);
                map.put(matchID, list);
            }
        }
        System.out.println("\n------ ITERATING HSM'S ------");
        for (List<QueryResult> queryResults : map.values()) {

            /* Sort high scoring matches by position on the contig */
            Collections.sort(queryResults, (q1, q2) -> q1.getValue()
                    .getSequencePos() - q2.getValue().getSequencePos());

            if (queryResults.size() > 10) {
                String queryMatch = queryResults.get(0).getValue().toString();
                int initalPos = queryResults.get(0).getValue().getSequencePos();


                System.out.println("list size: " + queryResults.size());

                /* Loop through sorted results, find gaps */
//                System.out.println("------ FINDING GAPS ------");
//                for (int i = 0; i < queryResults.size() - 1; i++) {
//                    /* compare position[i] and position[i+1] for significant gaps */
//                    int pos1 = queryResults.get(i).getValue().getSequencePos();
//                    int pos2 = queryResults.get(i + 1).getValue().getSequencePos();
//
//                /* TODO make threshold user configurable */
//                    if ((pos2 - pos1) > 100) {
//                        // exclude the match somehow
//                        break;
//                    } else {
//                        String match = queryResults.get(i + 1).toString();
//                        /* Grab the last characters from the next match */
//                        queryMatch += match.substring(match.length() - (pos2 - pos1));
//                    }
//                }
                ProteinSequence matchingSequence = new ProteinSequence(queryResults.get(0).getValue().getWholeSequece());
                matchingSequence.setSequenceID(queryResults.get(0).getValue().getSequenceID());
                matchingSequence.setSequencePos(initalPos);
                matchingSequence.setSequenceID(queryResults.get(0).getValue().getSequenceID());

                resultList.add(new QueryResult(tracker.getResults().get(0).getQuery(), matchingSequence));
            }
        }
        System.out.println("FOUND: " + resultList.size());
        timer = System.nanoTime() - timer;
        System.out.printf("Merge timer: %f s\n", timer / 1000000000.0);
        return resultList;
    }

    /**
     * Handles a storage request from a client.  This involves determining where
     * the data belongs via a {@link mendel.dht.partition.Partitioner}
     * implementation and then forwarding the data on to its destination.
     */
    @EventHandler
    public void handleStorageRequest(
            StorageRequest request, EventContext context)
            throws HashException, IOException, PartitionException {

        /* Determine where this block goes */
        Block file = request.getBlock();
        List<Metadata> dataList = file.getMetadata();
        List<byte[]> rawDataList = file.getData();

        HashMap<NodeInfo, Block> sequences = new HashMap<>();
        for (int i = 0; i < dataList.size(); ++i) {
            Metadata metadata = dataList.get(i);
            byte[] rawData = rawDataList.get(i);
            NodeInfo node = partitioner.locateData(metadata);
            Block entry = sequences.get(node);
            if (entry == null) {
                Block block = new Block(metadata, rawData);
                sequences.put(node, block);
            } else {
                entry.addData(metadata, rawData);
            }
        }
        for (Map.Entry<NodeInfo, Block> entry : sequences.entrySet()) {
            StorageEvent store = new StorageEvent(entry.getValue());
            sendEvent(entry.getKey(), store);
        }
    }

    @EventHandler
    public void handleStorage(StorageEvent store, EventContext context)
            throws FileSystemException, IOException {
        fileSystem.storeBlock(store.getBlock());
    }

    private void sendEvent(NodeInfo node, Event event)
            throws IOException {
        connectionPool.sendMessage(node, eventReactor.wrapEvent(event));
    }
}
