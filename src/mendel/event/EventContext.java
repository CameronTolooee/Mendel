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

import mendel.network.MendelMessage;
import mendel.network.NetworkDestination;

/**
 * Tracks the context of an event and allows retrieving event metadata.  Allows
 * replies to be sent directly to their originating source.
 *
 * @author malensek
 */
public class EventContext {

    private MendelMessage message;
    private EventWrapper wrapper;

    public EventContext(MendelMessage message, EventWrapper wrapper) {
        this.message = message;
        this.wrapper = wrapper;
    }

    /**
     * Send a reply back to the source that created the original event.
     */
    public void sendReply(Event e)
    throws IOException {
        MendelMessage m = wrapper.wrap(e);
        this.message.getContext().sendMessage(m);
    }

    /**
     * @return Server port number that this event was sent to.
     */
    public int getServerPort() {
        return message.getContext().getServerPort();
    }

    /**
     * @return NetworkDestination of the client that generated the event.
     */
    public NetworkDestination getSource() {
        return message.getContext().getSource();
    }
}
