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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides general system configuration information. The settings contained
 * within are guaranteed to not change during execution unless the reload()
 * method is called explicitly.
 * 
 * @author ctolooee
 */
public class SystemConfig {

    private static final Logger logger = Logger.getLogger("mendel");
    private static final String DEFAULT_HOMEDIR = ".";
    private static final String DEFAULT_STOREDIR = "/tmp/fs-mendel";

    /** Storage root */
    private static String rootDir;

    /** Configuration directory */
    private static String confDir;

    /** Mendel install directory (binaries, libraries) */
    private static String homeDir;

    /** Flag for determining whether to write out files */
    private static boolean psuedoFS;

    /**
     * Retrieves the system root directory. This directory is where Mendel
     * stores files.
     */
    public static String getRootDir() {
        return rootDir;
    }

    /**
     * Retrieves the system configuration directory, which contains all Mendel
     * configuration directives.
     */
    public static String getConfDir() {
        return confDir;
    }

    /**
     * Retrieves the Mendel installation directory, which contains the binaries,
     * scripts, and libraries required to run Mendel.
     */
    public static String getInstallDir() {
        return homeDir;
    }

    /**
     * Retrieves the flag that determines whether psuedoFS mode has been
     * enabled.
     */
    public static boolean getPseudoFS() {
        return psuedoFS;
    }

    /**
     * Reloads the Mendel system configuration.
     */
    public static void reload() {
        logger.log(Level.CONFIG, "Reloading system configuration");
        load();
    }

    /**
     * Loads all system configuration settings from the properties file.
     */
    private static void load() {
        Properties prop = new Properties();
        String propFile = System.getProperty("config.properties");
        InputStream is = null;
        try {
            is = new FileInputStream(new File(propFile));
            prop.load(is);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Unable to find properties file: "
                    + propFile + ". ");
            return;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to read properties file: "
                    + propFile + ". " + e);
            return;
        }

        String home = prop.getProperty("mendel.home.dir");
        if (home == null || home.equals("")) {
            logger.warning("Property mendel.home.dir not defined.");
            home = DEFAULT_HOMEDIR;
        }
        homeDir = home;
        logger.info("Mendel home directory set to: " + homeDir);

        confDir = homeDir.charAt(homeDir.length() - 1) == '/' ? homeDir + "conf"
                : homeDir + "/conf";
        logger.info("Mendel configuration directory set to: " + confDir);

        String storageDir = prop.getProperty("mendel.root.dir");
        if (storageDir == null || storageDir.equals("")) {
            logger.warning("Property mendel.root.dir not defined.");
            storageDir = DEFAULT_STOREDIR;
        }
        rootDir = storageDir;
        logger.info("Mendel file system storage root set to: " + rootDir);

        String psuedoProp = prop.getProperty("enable.psuedoFS");
        psuedoFS = Boolean.parseBoolean(psuedoProp);
        if(psuedoFS) {
            logger.info("PsuedoFS mode enabled");
        }
    }

    /**
     * Loads the System configuration information once.
     */
    static {
        load();
    }
}