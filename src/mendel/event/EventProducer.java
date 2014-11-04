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

package mendel.event;

import java.io.IOException;

import mendel.network.ClientMessageRouter;
import mendel.network.GalileoMessage;
import mendel.network.NetworkDestination;

/**
 * This class makes it easy to publish events from a client to a server by
 * linking a {@link ClientMessageRouter} instance to a {@link EventReactor}
 * instance.  This helps avoid message wrapping boilerplate every time an event
 * will be sent.
 *
 * @author malensek
 */
public class EventProducer {

    private ClientMessageRouter router;
    private EventReactor reactor;

    public EventProducer(ClientMessageRouter router, EventReactor reactor) {
        this.router = router;
        this.reactor = reactor;
    }

    /**
     * @param destination The server to publish the event to.
     * @param e Event to be published.
     */
    public void publishEvent(NetworkDestination destination, Event e)
    throws IOException {
        GalileoMessage m = reactor.wrapEvent(e);
        router.sendMessage(destination, m);
    }
}
