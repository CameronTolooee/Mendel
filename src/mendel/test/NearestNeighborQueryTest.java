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

package mendel.test;

import mendel.client.EventPublisher;
import mendel.comm.*;
import mendel.data.Metadata;
import mendel.event.BasicEventWrapper;
import mendel.fs.Block;
import mendel.network.ClientMessageRouter;
import mendel.network.MendelMessage;
import mendel.network.MessageListener;
import mendel.network.NetworkDestination;
import mendel.query.SimilarityQuery;
import mendel.query.QueryResult;
import mendel.serialize.SerializationException;
import mendel.util.PerformanceTimer;
import mendel.vptree.Kmer;

import java.io.IOException;
import java.util.UUID;

public class NearestNeighborQueryTest implements MessageListener {

    static PerformanceTimer pt = new PerformanceTimer("Nearest neighbor query");

    private String server;
    private int port;
    private ClientMessageRouter messageRouter;
    private static MendelEventMap eventMap = new MendelEventMap();
    private static BasicEventWrapper wrapper = new BasicEventWrapper(eventMap);

    public NearestNeighborQueryTest(String server, int port) throws IOException {
        this.server = server;
        this.port = port;
        messageRouter = new ClientMessageRouter();
        messageRouter.addListener(this);
    }

    public void disconnect() {
        messageRouter.shutdown();
    }

    public void query(String queryString) throws IOException, SerializationException {
        NetworkDestination dest = new NetworkDestination(server, port);
        SimilarityQuery query = new SimilarityQuery(queryString);
        QueryRequest qr = new QueryRequest(query, "test query");
        MendelMessage message = EventPublisher.wrapEvent(qr);
        messageRouter.sendMessage(dest, message);
    }

    public void store(Kmer seq) throws IOException {
        String uuid = UUID.nameUUIDFromBytes(seq.toString().getBytes()).toString();
        Metadata meta = new Metadata(seq, uuid);
        Block block = new Block(meta, seq.toString().getBytes());
        store(block);
    }

    public void store(Block block) throws IOException {
        NetworkDestination dest = new NetworkDestination(server, port);
        StorageRequest sr = new StorageRequest(block);
        MendelMessage message = EventPublisher.wrapEvent(sr);
        messageRouter.sendMessage(dest, message);
    }

    @Override
    public void onConnect(NetworkDestination endpoint) {
        System.out.println("Connected to " + endpoint);
    }

    @Override
    public void onDisconnect(NetworkDestination endpoint) {
        System.out.println("Disconnected from " + endpoint);
    }

    @Override
    public void onMessage(MendelMessage message) {
        try {
            QueryResponse response = (QueryResponse) wrapper.unwrap(message);
           // pt.stopAndPrint();
            //System.out.println(response.getResponse().size()
            //        + " results received");
            Kmer initalQuery = new Kmer(queryString);
            for (QueryResult block : response.getResponse()) {
                System.out.println(block.getValue().formatedOutput(initalQuery));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String queryString;

    public static void main(String[] args)
            throws IOException, SerializationException, InterruptedException {

        if (args.length < 3) {
            System.out.println("usage: mendel.test.FileUploadTest " +
                    "hostname port queryString");
            System.exit(1);
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        queryString = args[2];

        NearestNeighborQueryTest client =
                new NearestNeighborQueryTest(hostname, port);
        pt.start();
        /* Nearest neighbor  query */
        client.query(queryString);
    }
}
