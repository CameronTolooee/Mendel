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

package mendel.fs;

import mendel.data.Metadata;
import mendel.serialize.SerializationException;
import mendel.serialize.Serializer;
import mendel.util.PerformanceTimer;
import mendel.vptree.types.ProteinSequence;
import mendel.vptree.VPTree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MendelFileSystem implements FileSystem {

    private static Logger logger = Logger.getLogger("mendel");
    private RecoveryManager recoveryManager;
    private File storageDirectory;
    private boolean readOnly;
    private boolean pseudoFS;
    private static final String recoveryFileName = "mendel-metadata";

    VPTree<ProteinSequence> metadataTree;
    long count;

    public MendelFileSystem(String storageRoot, boolean pseudoFS)
            throws IOException, FileSystemException {
        this.pseudoFS = pseudoFS;
        initialize(storageRoot);
    }


    public MendelFileSystem(String storageRoot)
            throws FileSystemException, IOException {
        this(storageRoot, true);
    }

    protected void initialize(String storageRoot)
            throws FileSystemException, IOException {
        logger.info("Initializing Mendel File System.");
        if (pseudoFS) {
            logger.info("PsuedoFS enabled!");
        }
        logger.info("Storage directory: " + storageRoot);

        /* Ensure the storage and file system directories exists. */
        storageDirectory = new File(storageRoot);
        File fsDirectory = new File(storageRoot + "/fs/");
        if (!storageDirectory.exists()) {
            logger.warning("Root storage directory does not exist. " +
                    "Attempting to create.");
            if (!storageDirectory.mkdirs()) {
                throw new FileSystemException("Unable to create storage " +
                        "directory.");
            }
        }

        if (!fsDirectory.exists()) {
            logger.warning("Root file system directory does not exist. " +
                    "Attempting to create.");
            if (!fsDirectory.mkdirs()) {
                throw new FileSystemException("Unable to create file system " +
                        "directory.");
            }
        }
        logger.info("Free space: " + getFreeSpace());

        /* Verify permissions. */
        boolean read, write, execute;
        read = storageDirectory.canRead();
        write = storageDirectory.canWrite();
        execute = storageDirectory.canExecute();
        logger.info("File system permissions: " +
                (read ? 'r' : "") +
                (write ? 'w' : "") +
                (execute ? 'x' : ""));
        if (!read) {
            throw new FileSystemException("Cannot read storage directory.");
        }
        if (!execute) {
            throw new FileSystemException("Storage Directory " +
                    "is not Executable.");
        }
        readOnly = false;
        if (!write) {
            logger.warning("Storage directory is read-only. Starting " +
                    "file system in read-only mode.");
            readOnly = true;
        }

        recoveryManager = new RecoveryManager(storageDirectory
                + "/" + recoveryFileName);

        /* Initialize metadata vp-tree */
        initIndex();
    }

    private void initIndex() throws IOException {
        metadataTree = new VPTree<>();

        /* Attempt to recover the index from disk */
        metadataTree = recoveryManager.recover();
        recoveryManager.start();

        if (metadataTree == null) {
            logger.log(Level.SEVERE, "Failed to recover path journal!");
            recoveryManager.erase();
            recoveryManager.start();
            metadataTree = new VPTree<>();
        }
    }

    /**
     * Scans a directory (and its subdirectories) for blocks.
     *
     * @param directory Directory to scan for blocks.
     * @return ArrayList of String paths to blocks on disk.
     */
    protected List<String> scanDirectory(File directory) {
        List<String> blockPaths = new ArrayList<>();
        scanSubDirectory(directory, blockPaths);
        return blockPaths;
    }

    /**
     * Scans a directory (and its subdirectories) for blocks.
     *
     * @param directory Directory file descriptor to scan
     * @param fileList  ArrayList of Strings to populate with FileBlock paths.
     */
    private void scanSubDirectory(File directory, List<String> fileList) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanSubDirectory(file, fileList);
                continue;
            }
            String fileName = file.getAbsolutePath();
            if (fileName.endsWith(FileSystem.BLOCK_EXTENSION)) {
                fileList.add(fileName);
            }
        }
    }

    /**
     * Does a full recovery from disk; this scans every block in the system,
     * reads metadata, and performs a checksum to verify integrity. If not
     * already obvious, this could be very slow.
     */
    protected void fullRecovery() {
        logger.warning("Performing full recovery from disk");
        List<String> blockPaths = rebuildPaths(storageDirectory);
        recover(blockPaths);
    }

    /**
     * Scans the directory structure on disk to find all the blocks stored.
     *
     * @param storageDir the root directory to start scanning from.
     */
    protected List<String> rebuildPaths(File storageDir) {
        PerformanceTimer rebuildTimer = new PerformanceTimer();
        rebuildTimer.start();
        logger.info("Recovering path index");
        List<String> blockPaths = scanDirectory(storageDir);
        rebuildTimer.stop();
        logger.info("Index recovery took "
                + rebuildTimer.getLastResult() + " ms.");
        return blockPaths;
    }

    /**
     * Does a full recovery from disk on a particular Mendel partition; this
     * scans every block in the partition, reads its metadata, and performs a
     * checksum to verify block integrity.
     */
    protected void recover(List<String> blockPaths) {
        PerformanceTimer recoveryTimer = new PerformanceTimer();
        recoveryTimer.start();
        logger.info("Recovering metadata and building graph");
        long counter = 0;
        for (String path : blockPaths) {
            try {
                Metadata metadata = loadMetadata(path);
                storeMetadata(metadata, path);
                ++counter;
                if (counter % 10000 == 0) {
                    logger.info(String.format("%d blocks scanned, " +
                                    "recovery %.2f%% complete.", counter,
                            ((float) counter / blockPaths.size()) * 100));
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to recover metadata " +
                        "for block: " + path, e);
            }
        }
        recoveryTimer.stop();
        logger.info("Recovery operation complete. Time: "
                + recoveryTimer.getLastResult() + " ms.");
    }

    @Override
    public Block loadBlock(String blockPath)
            throws IOException, SerializationException {
        Block block = Serializer.restore(Block.class, blockPath);
        return block;
    }

    @Override
    public Metadata loadMetadata(String blockPath)
            throws IOException, SerializationException {

        /* We can just load the block as usual, but only perform the
        * deserialization on the Metadata. Metadata is stored as the first
        * item in a serialized Block instance. */
        return Serializer.restore(Metadata.class, blockPath);
    }

    @Override
    public String storeBlock(Block block)
            throws FileSystemException, IOException {
        String blockPath = "";
        for (int i = 0; i < block.getMetadata().size(); ++i) {
            ++count;
            String name = block.getMetadata().get(i).getName();
            if (name.equals("")) {
                UUID blockUUID = UUID.nameUUIDFromBytes(block.getData().get(i));
                name = blockUUID.toString();
            }
            blockPath = storageDirectory + "/fs/" + name
                    + FileSystem.BLOCK_EXTENSION;

        /* Add metadata to the in-memory map */
            //metadataMap.put(block.getMetadata().getSeqBlock(), blockPath);
            metadataTree.add(block.getMetadata().get(i).getSequence());

        /* Don't write data to disk if pseudoFS is enabled */
            if (!pseudoFS) {
                byte[] blockData = Serializer.serialize(block);
                FileOutputStream blockOutStream = new FileOutputStream(blockPath);
                blockOutStream.write(blockData);
                blockOutStream.close();
            }
        }
        return blockPath;
    }

    @Override
    public void storeMetadata(Metadata metadata, String blockPath)
            throws FileSystemException, IOException {

    }

    /**
     * Reports whether the Mendel filesystem is read-only.
     *
     * @return true if the filesystem is read-only.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Reports the amount of free space (in bytes) in the root storage
     * directory.
     *
     * @return long integer with the amount of free space, in bytes.
     */
    public long getFreeSpace() {
        return storageDirectory.getFreeSpace();
    }

    /**
     * Performs a clean shutdown of the FileSystem instance. This includes
     * flushing buffers, writing changes out to disk, persisting index
     * structures, etc.
     * <p/>
     * Note that this method may be called during a signal handling operation,
     * which may mean that the logging subsystem has already shut down, so
     * critical errors/information should be printed to stdout or stderr.
     * Furthermore, there is no guarantee all the shutdown operations will be
     * executed, so time is of the essence here.
     */
    public void shutdown() throws IOException, FileSystemException {
        recoveryManager.writeIndex(metadataTree);
    }

    public List<ProteinSequence> nearestNeighboQuery(String query) {
        /* TODO  Rename query classes to be more accurate/insightful */
        List<ProteinSequence> result = new ArrayList<>();
            result.addAll(nearestNeighborQuery(query));
        return result;
    }


    public List<ProteinSequence> nearestNeighborQuery(String queryString) {
        ProteinSequence sequence = new ProteinSequence(queryString);
        return metadataTree.getNearestNeighbors(sequence, 5);
    }

    public long countBlocks() {
        return count;
    }

    /**
     * Searches the root file system for the file generated by the query.
     *
     * @param querySequence they query containing data to generate filename
     * @return the Block of the file data if it exists; null otherwise
     * @throws IOException
     * @throws SerializationException
     */
//    public Block retrieveSequence(String querySequence)
//            throws IOException, SerializationException {
//        /* If pseudoFS mode enabled, nothing on disk; rely on metadata */
//        if(pseudoFS) {
//            return new Block(path.getBytes(), path);
//        } else {
//            File f = new File(path);
//            if (f.exists() && !f.isDirectory()) {
//                return loadBlock(path);
//            } else {
//                return null;
//            }
//        }
//    }
}
