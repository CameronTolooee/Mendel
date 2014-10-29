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

package mendel.util;

/**
 * Utility for managing and displaying Mendel software versions and splash art.
 * 
 * @author ctolooee
 * 
 */
public class Version {

    private final static Version version = new mendel.util.Version();
    private final static Package pkg = version.getClass().getPackage();

    public static final String NAME = "Mendel";
    public static final String VERSION = pkg.getSpecificationVersion();

    public Version() {
    }

    /**
     * Gets the version number of the running version of Mendel
     * 
     * @return Current Mendel version number
     */
    public static String getVersion() {
        return (NAME + " " + VERSION);
    }

    /**
     * Print Mendel version information.
     */
    public void printVersionInformation() {
        System.out.println(getVersion());
    }

    /**
     * Print the Mendel splash art and version information.
     */
    public static void printSplash() {
        System.out.println();

        System.out.println("                          _      _  ");
        System.out.println("     /\\/\\   ___ _ __   __| | ___| | ");
        System.out.println("    /    \\ / _ \\ '_ \\ / _` |/ _ \\ | ");
        System.out.println("   / /\\/\\ \\  __/ | | | (_| |  __/ | ");
        System.out.println("   \\/    \\/\\___|_| |_|\\__,_|\\___|_| ");
        System.out.println();
        System.out.println("             Version  " + VERSION);
        System.out.println();
    }
}
