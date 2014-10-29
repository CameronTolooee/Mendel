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
 * A tool to show progression of a task with a graphical bar and estimated time
 * remaining.
 * 
 * @author ctolooee
 * 
 */
public class ProgressBar {
    private int max;
    private int current;
    private String name;
    private long start;
    private long lastUpdate;

    /**
     * Create a progression bar with the specified name and max value.
     * 
     * @param max
     *            the maximum value of progress (i.e. what value equates to 100%
     *            completion)
     * @param name
     *            the name of the bar to be displayed while updating.
     */
    public ProgressBar(int max, String name) {
        this.start = System.currentTimeMillis();
        this.name = name;
        this.max = max;
        System.out.println(this.name + ":");
        this.printBar(false);
    }

    /**
     * Set the current progress value to the specified argument. Use this to
     * update the progress bar to represent <code>i/max</code> completion.
     * 
     * @param i
     *            the current value of completion (relative to max)
     */
    public void update(int i) {
        this.current = i;
        if ((System.currentTimeMillis() - this.lastUpdate) > 1000) {
            this.lastUpdate = System.currentTimeMillis();
            this.printBar(false);
        }
    }

    /**
     * Display the completed progression bar. To be called once the task being
     * tracked is completed.
     */
    public void finish() {
        this.current = this.max;
        this.printBar(true);
    }

    private void printBar(boolean finished) {
        double numbar = Math.floor(20 * (double) current / (double) max);
        String strbar = "";
        int ii = 0;
        for (ii = 0; ii < numbar; ii++) {
            strbar += "=";
        }
        for (ii = (int) numbar; ii < 20; ii++) {
            strbar += " ";
        }
        long elapsed = (System.currentTimeMillis() - this.start);
        int seconds = (int) (elapsed / 1000) % 60;
        int minutes = (int) (elapsed / 1000) / 60;
        String strend = String.format("%02d", minutes) + ":"
                + String.format("%02d", seconds);

        String strETA = "";
        if (elapsed < 2000) {
            strETA = "--:--";
        } else {
            long timeETA = elapsed * (long) ((double) max / (double) current);
            int ETAseconds = (int) (timeETA / 1000) % 60;
            int ETAminutes = (int) (timeETA / 1000) / 60;
            strETA = String.format("%02d", ETAminutes) + ":"
                    + String.format("%02d", ETAseconds);
        }
        if (finished) {
            strend = "Finished: " + strend + "               ";
        } else {
            strend = "Elapsed: " + strend + " ETA: " + strETA + "   ";
        }
        System.out.print("|" + strbar + "| " + strend);
        if (finished) {
            System.out.print("\n");
        } else {
            System.out.print("\r");
        }
    }

    /* Usage example */
    public static void main(String args[]) throws Exception {
        ProgressBar pb = new ProgressBar(100, "TEST");
        for (int i = 0; i < 100; ++i) {
            pb.update(i);
            Thread.sleep(100);
        }
        pb.finish();
    }

}