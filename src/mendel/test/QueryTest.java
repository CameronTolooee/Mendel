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

import mendel.data.Metadata;
import mendel.fs.Block;
import mendel.fs.MendelFileSystem;
import mendel.query.ExactQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class QueryTest {
    public static void main(String[] args) throws Exception {
        List<Block> blocks = new ArrayList<>();

        MendelFileSystem gfs =
                new MendelFileSystem("C:\\Users\\Ctolooee\\Desktop\\fs");

        for (int i = 0; i < 10; i++) {
            blocks.add(generateBlock());
        }
        String seq = "CCCCCCCCC";
        String uuid = UUID.nameUUIDFromBytes(seq.getBytes()).toString();
        Metadata meta = new Metadata(seq, uuid);
        blocks.add(new Block(meta, seq.getBytes()));

        /* Insert the blocks we've loaded, if any */
        if (blocks.size() > 0) {
            for (Block block : blocks) {
                gfs.storeBlock(block);
            }
        }

        /* Execute some queries */
        ExactQuery q = new ExactQuery("CCCCCCCCC");
        System.out.println("Query: " + q);
        Block result = gfs.query(q);
        System.out.println(result.getMetadata().getName());

        gfs.shutdown();
    }

    private static Block generateBlock() {
        String[] chars = {"A", "C", "T", "G"};
        Random rand = new Random();
        String sequence = "";
        for (int i = 0; i < 9; i++) {
            sequence += chars[rand.nextInt(4)];
        }
        Metadata meta = new Metadata(sequence,
                UUID.nameUUIDFromBytes(sequence.getBytes()).toString());
        return new Block(meta, sequence.getBytes());
    }
}
