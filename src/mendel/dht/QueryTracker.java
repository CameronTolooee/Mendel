/*
Copyright (c) 2015, Colorado State University
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

package mendel.dht;

import mendel.event.EventContext;
import mendel.query.QueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks query ids to its originating source while being passed around the
 * system. Also maintains query metadata such as intermediate results and sub-
 * query counters to ensure synchronization of responses.
 *
 * @author ctolooee
 */
public class QueryTracker {

    private static final Object counterLock = new Object();
    private final Object sendRecvLock = new Object();
    private final Object resultsLock = new Object();

    private List<QueryResult> results;
    private int sendRecvCount;
    private static long queryCounter = 0;
    private long queryId;
    private EventContext context;

    public QueryTracker(EventContext context) {
        synchronized (counterLock) {
            this.queryId = QueryTracker.queryCounter++;
        }
        synchronized(sendRecvLock) {
            sendRecvCount = 0;
        }
        this.results = new ArrayList<>();
        this.context = context;
    }

    public void incrementSendRecvCount() {
        synchronized (sendRecvLock) {
            ++sendRecvCount;
        }
    }

    public void decrementSendRecvCount() {
        synchronized (sendRecvLock) {
            --sendRecvCount;
        }
    }

    public int getSendRecvCount() {
        int retval = -1;
        synchronized (sendRecvLock) {
            retval = sendRecvCount;
        }
        return retval;
    }

    public long getQueryId() {
        return queryId;
    }

    public String getIdString(String sessionId) {
        return sessionId + "$" + queryId;
    }

    public EventContext getContext() {
        return context;
    }

    public void addResults(List<QueryResult> response) {
        synchronized (resultsLock) {
            results.addAll(response);
        }
    }

    public List<QueryResult> getResults() {
        return results;
    }
}