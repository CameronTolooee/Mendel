/*
Copyright (c) 2013, Colorado State University
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

/**
 * Interface for classes that will listen for incoming {@link mendel.network.GalileoMessage}
 * instances produced by a {@link MessageRouter}.
 *
 * @author malensek
 */
public interface MessageListener {

    /**
     * Called when a message is ready to be processed.  This method is invoked
     * by a Selector thread (from a {@link MessageRouter} instance).  To avoid
     * hurting performance, the implementation of onMessage must be lightweight.
     * In fact, blocking in this method implementation can cause serious
     * problems.
     * <p>
     * An example use case could involve placing incoming messages in a blocking
     * queue and then having another thread process them.  If the queue gets too
     * full, then it may be appropriate to block here (thus blocking the
     * Selector thread) to slow the rate of incoming messages from the network.
     *
     * @param message GalileoMessage that was received; null if the connection
     * has been terminated.
     */
    public void onMessage(GalileoMessage message);

    /**
     * Called when a connection is established with a remote endpoint.
     */
    public void onConnect(NetworkDestination endpoint);

    /**
     * Called when the MessageListener has been disconnected from a remote
     * endpoint.
     */
    public void onDisconnect(NetworkDestination endpoint);
}
