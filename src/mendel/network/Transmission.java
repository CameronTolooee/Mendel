/*
Copyright (c) 2014, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package mendel.network;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents the transmission of one unit of data ({@link MendelMessage}).
 * Apart from managing the data associated with a network transmission, this
 * class also acts like a Future: the finish() method will cause the calling
 * thread to block until the transmission has been completed (either
 * successfully or unsuccessfully).  This allows transmissions to be carried out
 * asynchronously, but gives clients a means to ensure the transmission
 * finished.  This class also allows clients to retrieve Exceptions that may
 * have occurred while sending the message.
 *
 * @author malensek
 */
public class Transmission {

    private static final Object lock = new Object();
    private boolean finished = false;

    private Queue<Exception> exceptions = new LinkedList<>();

    private ByteBuffer payload;

    protected Transmission(ByteBuffer payload) {
        this.payload = payload;
    }

    protected ByteBuffer getPayload() {
        return payload;
    }

    /**
     * Causes the calling thread to wait until this transmission has completed.
     *
     * @return true if the transmission finished without exceptions.
     */
    public boolean finish()
    throws InterruptedException {
        if (finished == true) {
            return (hasException() == false);
        }

        synchronized (lock) {
            while (finished == false) {
                lock.wait();
            }
        }

        return (hasException() == false);
    }

    /**
     * Updates the status of this transmission to completed and notifies any
     * waiting threads.
     */
    protected void setFinished() {
        synchronized (lock) {
            finished = true;
            lock.notifyAll();
        }
    }

    /**
     * Associates an Exception with this Tranmission to indicate errors or
     * other information specific to the Tranmission.
     */
    protected void addException(Exception e) {
        exceptions.add(e);
    }

    public boolean hasException() {
        return (exceptions.isEmpty() == false);
    }

    public Queue<Exception> getExceptions() {
        return exceptions;
    }
}
