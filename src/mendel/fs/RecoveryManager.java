/*
 * Copyright (c) 2015, Colorado State University All rights reserved.
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

package mendel.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import mendel.serialize.SerializationException;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;
import mendel.util.PerformanceTimer;
import mendel.vptree.VPTree;

public class RecoveryManager {

    private static final Logger logger = Logger.getLogger("mendel");

    private String indexFile;

    private DataOutputStream indexStore;

    private Map<String, Integer> featureNames = new HashMap<>();
    private int nextId = 1;

    private boolean running = false;

    public RecoveryManager(String pathFile) {
        this.indexFile = pathFile + ".index";
    }

    /**
     * Recovers the Path Journal from disk.
     * <p/>
     * /@param paths A list that will be populated with all the recovered paths.
     *
     * @return true if the recovery was completed cleanly; if false, there were
     * issues with the journal files (possible corruption).
     */
    public VPTree recover()
            throws IOException {
        PerformanceTimer timer = new PerformanceTimer();
        timer.start();

        if (new File(indexFile).exists() == false) {
            erase();
            return null;
        }
        VPTree vpTree  = null;
        try {
            vpTree = recoverIndex();
        } catch (EOFException e) {
            logger.info("Reached end of recovery index.");
        } catch (FileNotFoundException e) {
            logger.info("Could not locate index.  Index recovery is "
                    + "not possible.");
            return null;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading path index!", e);
        } catch (SerializationException e) {
            logger.log(Level.WARNING, "Error deserializng the index from disk", e);
        }
        timer.stop();
        logger.log(Level.INFO, "Finished recovery in "
                + timer.getLastResult() + " ms.");
        return vpTree;
    }

    /**
     * Recovers vp-tree index stored on disk.
     */
    private VPTree recoverIndex()
            throws IOException, SerializationException {
        DataInputStream pathIn = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(indexFile)));

        long checksum = pathIn.readLong();
        int indexSize = pathIn.readInt();

        byte[] pathBytes = new byte[indexSize];
        int read = pathIn.read(pathBytes);
        if (read != indexSize) {
            logger.info("Reached end of path index");
        }

        CRC32 crc = new CRC32();
        crc.update(pathBytes);
        if (crc.getValue() != checksum) {
            logger.warning("Detected checksum mismatch; ignoring path.");
        }
        pathIn.close();

        return deserializeTree(pathBytes);
    }

    /**
     * Prepares the journal files and allows new entries to be written.
     */
    public void start()
            throws IOException {
        OutputStream indexOut = Files.newOutputStream(Paths.get(indexFile),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND,
                StandardOpenOption.DSYNC);
        indexStore = new DataOutputStream(new BufferedOutputStream(indexOut));

        running = true;
    }


    /**
     * Adds a vp-tree {@link mendel.vptree.VPTree} to the index journal.
     *
     * @param vpTree The vp-tree to add to the journal.
     */
    public void writeIndex(VPTree vpTree)
            throws FileSystemException, IOException {
        if (running == false) {
            throw new FileSystemException("Path Journal has not been started!");
        }

        byte[] pathBytes = serializePath(vpTree);

        CRC32 crc = new CRC32();
        crc.update(pathBytes);
        long check = crc.getValue();

        indexStore.writeLong(check);
        indexStore.writeInt(pathBytes.length);
        indexStore.write(pathBytes);
        indexStore.flush();
    }

    /**
     * Given a {@link mendel.vptree.VPTree}, this method serializes the data to
     * a byte array that can be appended to the index journal.
     */
    private byte[] serializePath(VPTree vpTree)
            throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        SerializationOutputStream sOut = new SerializationOutputStream(bOut);
        vpTree.serialize(sOut);
        sOut.close();
        return bOut.toByteArray();
    }

    /**
     * Deserializes a {@link mendel.vptree.VPTree} from a byte array.
     */
    private VPTree deserializeTree(byte[] pathBytes)
            throws IOException, SerializationException {
        SerializationInputStream sIn = new SerializationInputStream(
                new ByteArrayInputStream(pathBytes));
        VPTree vpIndex = new VPTree(sIn);
        sIn.close();
        return vpIndex;
    }

    /**
     * Removes the Index Journal.  This method shuts
     * the RecoveryManager down before deleting the files.
     */
    public void erase()
            throws IOException {
        shutdown();
        new File(indexFile).delete();
    }

    /**
     * Closes open index files and stops accepting new data.
     */
    public void shutdown()
            throws IOException {
        if (running == false) {
            return;
        }
        indexStore.close();
    }
}