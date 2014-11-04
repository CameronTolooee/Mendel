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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Extends the standard {@link ClientMessageRouter} by aging out connections
 * over time and caching frequently-used connections.  This modification is
 * well-suited for environments with high counts of outgoing connections.
 *
 * @author malensek
 */
public class ClientConnectionPool extends ClientMessageRouter {

    protected Map<NetworkDestination, Long> connectionAge = new HashMap<>();

    public ClientConnectionPool()
    throws IOException {
        super();
    }

    @Override
    public Transmission sendMessage(NetworkDestination destination,
            GalileoMessage message)
    throws IOException {
        Transmission trans = super.sendMessage(destination, message);
        connectionAge.put(destination, System.nanoTime());
        return trans;
    }
}
