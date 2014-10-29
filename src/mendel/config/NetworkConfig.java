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

package mendel.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mendel.network.GroupInfo;
import mendel.network.NetworkInfo;
import mendel.network.NodeInfo;

/**
 * Reads and maintains Mendel network configuration from the installation on
 * disk. Groups are automatically configured if no manual configuration is
 * provided.
 * 
 * @author ctolooee
 * 
 */
public class NetworkConfig {
    private static final Logger logger = Logger.getLogger("galileo");

    public static final int DEFAULT_PORT = 5555;
    public static final String NODES_FILE_NAME = "nodes";

    /**
     * Reads a network description directory from disk.
     * 
     * @param directory
     *            full path name of the network description directory.
     * 
     * @return NetworkInfo containing the network information read from the
     *         directory.
     * @throws FileNotFoundException, IOException 
     */
    public static NetworkInfo readNetworkDescription(String directory)
            throws FileNotFoundException, IOException {

        /* Add a trailing slash if needed */
        directory = directory.charAt(directory.length() - 1) == '/' ? directory
                : directory + "/";

        File nodesFile = new File(directory);
        if (!nodesFile.exists()) {
            throw new FileNotFoundException("Could not find network "
                    + "configuration file: " + nodesFile.getAbsolutePath());
        }

        return readNodesFile(nodesFile);
    }

    /**
     * Read host:port pairs from a group description file (*.group).
     * 
     * @param file
     *            File containing the group members.
     * 
     * @return GroupInfo containing the hosts read from file.
     * @throws IOException 
     */
    public static NetworkInfo readNodesFile(File file)
            throws IOException {
        NetworkInfo network = new NetworkInfo();

        FileReader fReader = new FileReader(file);

        BufferedReader reader = new BufferedReader(fReader);
        int lineNum = 0;
        String line;
        String groupName = null;
        GroupInfo group = null;
        while ((line = reader.readLine()) != null) {
            ++lineNum;
            line = line.trim().replaceAll("\\s", "");
            if (line.startsWith("#") || line.equals("")) {
                continue;
            } else if (line.startsWith("$")) {
                if (group != null && group.getAllNodes().size() != 0) {
                    network.addGroup(group);
                }
                groupName = line.substring(1, line.length());
                group = new GroupInfo(groupName);
            }

            String[] hostInfo = line.split(":", 2);
            String nodeName = hostInfo[0];
            if (nodeName.equals("")) {
                logger.warning("Could not determine StorageNode "
                        + "hostname for group '" + groupName + "' on line "
                        + lineNum + "; ignoring entry.");

                continue;
            }

            if (hostInfo.length <= 1) {
                /* No port specified; use the default port. */
                NodeInfo node = new NodeInfo(nodeName, DEFAULT_PORT);
                group.addNode(node);
            } else {
                /*
                 * A port, or list of several comma-separated ports, has been
                 * specified.
                 */
                String portStr = hostInfo[1];
                String ports[] = portStr.split(",");
                for (String portEntry : ports) {
                    try {
                        int port = Integer.parseInt(portEntry);
                        group.addNode(new NodeInfo(nodeName, port));
                    } catch (NumberFormatException e) {
                        logger.log(Level.WARNING, "Could not parse "
                                + "StorageNode port number on line " + lineNum
                                + ";  ignoring entry.", e);
                    }
                }
            }
        }
        reader.close();

        return network;
    }
}
