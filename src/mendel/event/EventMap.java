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

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a base implementation for mapping event identifiers to classes, and
 * classes back to event identifiers.
 *
 * @author malensek
 */
public class EventMap {

    protected final Map<Integer, Class<? extends Event>>
        intToClass = new HashMap<>();
    protected final Map<Class<? extends Event>, Integer>
        classToInt = new HashMap<>();

    public EventMap() { }

    /**
     * Maps an event identifier to a particular Event class implementation.
     * @param id Event Identifier
     * @param clazz Class to map to id
     * @throws IllegalArgumentException if the event id provided has already
     * been mapped to an Event implementation.
     */
    protected void addMapping(int id, Class<? extends Event> clazz) {
        if (intToClass.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Event id has already been mapped!");
        }
        intToClass.put(id, clazz);
        classToInt.put(clazz, id);
    }

    /**
     * Maps an Event class implementation to an automatically-generated event
     * identifier.  This method is useful for applications with a small number
     * of events whose identifiers can change over time, but should generally
     * not be used in production settings.
     * @param clazz Class to map to id
     * @throws IllegalArgumentException if the event id provided has already
     * been mapped to an Event implementation.
     */
    protected void addMapping(Class<? extends Event> clazz) {
        addMapping(intToClass.size(), clazz);
    }

    /**
     * Retrieves the class associated with the provided event identifier.
     * @param id Event identifier to retrieve an Event implementation for.
     */
    public Class<? extends Event> getClass(int id) {
        return intToClass.get(id);
    }

    /**
     * Retrieves the event identifier associated with a given Event class.
     * @param clazz The class to retrieve the event identifier for.
     */
    public int getInt(Class<?> clazz) {
        return classToInt.get(clazz);
    }
}
