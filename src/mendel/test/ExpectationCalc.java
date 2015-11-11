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
import mendel.comm.MendelEventMap;
import mendel.comm.QueryRequest;
import mendel.comm.QueryResponse;
import mendel.comm.StorageRequest;
import mendel.config.NetworkConfig;
import mendel.data.Metadata;
import mendel.event.BasicEventWrapper;
import mendel.fs.Block;
import mendel.network.*;
import mendel.query.SimilarityQuery;
import mendel.query.QueryResult;
import mendel.serialize.SerializationException;
import mendel.util.PerformanceTimer;
import mendel.util.SmithWaterman;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava.nbio.core.sequence.io.FastaReader;
import org.biojava.nbio.core.sequence.io.GenericFastaHeaderParser;
import org.biojava.nbio.core.sequence.io.ProteinSequenceCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * The benchmarking code to time the same query over the same dataset on
 * various cluster sizes. This benchmark shows the performance scalability.
 *
 * @author ctolooee
 */
public class ExpectationCalc implements MessageListener {

    private String server;
    private int port;
    private ClientMessageRouter messageRouter;
    private static MendelEventMap eventMap = new MendelEventMap();
    private static BasicEventWrapper wrapper = new BasicEventWrapper(eventMap);
    public static PerformanceTimer queryTimer = new PerformanceTimer("Query times");
    public static final Object queryLock = new Object();
    public HashMap<Double, Integer> scoreCount = new HashMap<>();

    public ExpectationCalc(String server, int port) throws IOException {
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
        SimilarityQuery query = new SimilarityQuery(queryString, queryString);
        QueryRequest qr = new QueryRequest(query, "test query");
        MendelMessage message = EventPublisher.wrapEvent(qr);
        messageRouter.sendMessage(dest, message);
    }

    public void store(String seq) throws IOException {
        String uuid = UUID.nameUUIDFromBytes(seq.getBytes()).toString();
        Metadata meta = new Metadata(new mendel.vptree.types.ProteinSequence(seq), uuid);
        Block block = new Block(meta, seq.getBytes());
        store(block);
    }

    public void store(Block block) throws IOException {
        NetworkDestination dest = new NetworkDestination(server, port);
        StorageRequest sr = new StorageRequest(block);
        MendelMessage message = EventPublisher.wrapEvent(sr);
        messageRouter.sendMessage(dest, message);
    }

    public void store(mendel.vptree.types.ProteinSequence seq, NetworkDestination dest) throws IOException {
        String uuid = UUID.nameUUIDFromBytes(seq.toString().getBytes()).toString();
        Metadata meta = new Metadata(seq, uuid);
        Block block = new Block(meta, seq.toString().getBytes());
        StorageRequest sr = new StorageRequest(block);
        MendelMessage message = EventPublisher.wrapEvent(sr);
        messageRouter.sendMessage(dest, message);
    }

    public void store(List<mendel.vptree.types.ProteinSequence> seqs, NetworkDestination dest) throws IOException {
        String uuid = UUID.nameUUIDFromBytes(seqs.get(0).toString().getBytes()).toString();
        Metadata meta = new Metadata(seqs.get(0), uuid);
        Block block = new Block(meta, seqs.get(0).toString().getBytes());
        for (int i = 1; i < seqs.size(); ++i) {
            mendel.vptree.types.ProteinSequence seq = seqs.get(i);
            uuid = UUID.nameUUIDFromBytes(seq.toString().getBytes()).toString();
            meta = new Metadata(seq, uuid);
            block.addData(meta, seq.toString().getBytes());
        }
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
            queryTimer.stopAndPrint();



            String query = response.getQuery();
            ArrayList<SmithWaterman>
                    sortedList = new ArrayList<>();

            for (QueryResult block : response.getResponse()) {
                String subject = block.getValue().getWord();
                SmithWaterman sw = new SmithWaterman("Query", query,
                        block.getValue().getSequenceID(), subject);
                sortedList.add(sw);
            }

            Collections.sort(sortedList);
            Double score = sortedList.get(1).getScore();
            Integer entry = scoreCount.get(score);
            if(entry == null) {
                scoreCount.put(score, 1);
            } else {
                scoreCount.put(score, entry + 1);
            }

            synchronized (queryLock) {
                queryLock.notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void queryData(String queryFile, ExpectationCalc client) {
        File file = new File(queryFile);
        Scanner sc;
        try {
            sc = new Scanner(file);
            String query = "";
            while (sc.hasNext()) {
                String line = sc.nextLine();
                if(line.isEmpty()) {
                    queryTimer.start();
                    client.query(query);
                    query = "";
                    synchronized (queryLock) {
                        queryLock.wait();
                    }
                } else if(!line.startsWith(">")) {
                    query += line;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void indexData(String dbFile, ExpectationCalc client,
                                  PerformanceTimer indexTimer) throws IOException {


        NetworkInfo network = NetworkConfig.readNodesFile(new File("/s/chopin/k/grad/ctolooee/Research/Mendel/conf/nodes"));

        List<NodeInfo> list = network.getAllNodes();

        indexTimer.start();
        int count = 0;

        NetworkDestination dest;
        List<mendel.vptree.types.ProteinSequence> batch = new ArrayList<>();

        FileInputStream inStream = new FileInputStream(dbFile);
        FastaReader<ProteinSequence, AminoAcidCompound> fastaReader =
                new FastaReader<>(inStream,
                        new GenericFastaHeaderParser<>(),
                        new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
        LinkedHashMap<String, ProteinSequence> b = fastaReader.process();
        int window_size = 30;
        for (Map.Entry<String, ProteinSequence> entry : b.entrySet()) {
            String seq = entry.getValue().getSequenceAsString();

            int len = seq.length();
            for (int i = 0; i + window_size < len; ++i) {
                while (i + window_size < len && batch.size() < 500) {
                    mendel.vptree.types.ProteinSequence sequence =
                            new mendel.vptree.types.ProteinSequence(
                                    seq.substring(i, i + window_size));
                    sequence.setSequenceID(entry.getValue().getOriginalHeader());
                    sequence.setWholeSequence(seq);
                    batch.add(sequence);
                    ++i;
                }
                NodeInfo info = list.get(count++ % list.size());
                dest = new NetworkDestination(info.getHostname(), info.getPort());
                client.store(batch, dest);
                batch.clear();
            }
        }
        indexTimer.stopAndPrint();
    }

    public static void main(String[] args) throws IOException,
            InterruptedException, SerializationException {
        if (args.length < 5) {
            System.out.println("Usage: mendel.test.ExpectationCalc " +
                    "host port db_file query_file repeat");
            System.exit(1);
        }

        PerformanceTimer indexTimer = new PerformanceTimer("Indexing time");
        String server = args[0];
        int port = Integer.parseInt(args[1]);
        ExpectationCalc client = new ExpectationCalc(server, port);
        if (!args[2].equals("-")) {
            indexData(args[2], client, indexTimer);
        }
        int repeat = Integer.parseInt(args[4]);
        for (int i = 0; i < repeat; ++i) {
            if (!args[3].equals("-")) {
                queryData(args[3], client);
            }
        }
        client.scoreCount.forEach((key, value) -> System.out.println(key + ", " + value));

                client.disconnect();
    }
}
