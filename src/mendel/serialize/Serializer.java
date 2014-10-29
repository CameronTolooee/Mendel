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

package mendel.serialize;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * This class provides convenience functions to make the Serialization and
 * Deserialization process easier.
 *
 * In brief, the static methods in this class will initialize proper streams for
 * reading or creating objects, do the work, and then close the streams.
 * 
 * @author malensek
 */
public class Serializer {

    /**
     * Dumps a ByteSerializable object to a portable byte array.
     *
     * @param obj The ByteSerializable object to serialize.
     *
     * @return binary byte array representation of the object.
     */
    public static byte[] serialize(ByteSerializable obj)
    throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        BufferedOutputStream buffOut = new BufferedOutputStream(byteOut);

        SerializationOutputStream serialOut =
            new SerializationOutputStream(buffOut);

        serialOut.writeSerializable(obj);
        serialOut.close();
        return byteOut.toByteArray();
    }

    /**
     * Loads a ByteSerializable object's binary form and then instantiates a new
     * object using the SerializationInputStream constructor.
     *
     * @param type The type of object to create (deserialize).
     *             For example, Something.class.
     *
     * @param bytes Binary form of the object being loaded.
     */
    public static <T extends ByteSerializable> T
        deserialize(Class<T> type, byte[] bytes)
    throws IOException, SerializationException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        BufferedInputStream buffIn = new BufferedInputStream(byteIn);

        SerializationInputStream serialIn =
            new SerializationInputStream(buffIn);

        T obj = deserialize(type, serialIn);
        serialIn.close();

        return obj;
    }

    /**
     * Loads a ByteSerializable object's binary form from an input stream and
     * then instantiates a new object using the SerializationInputStream
     * constructor.  This method is private to enforce users of the
     * Serialization framework to instantiate deserializable objects using a
     * SerializationInputStream constructor.
     *
     * @param type The type of object to create (deserialize).
     *             For example, Something.class.
     *
     * @param in SerializationInputStream containing a serialized instance of
     *           the object being loaded.
     */
    private static <T extends ByteSerializable> T deserialize(Class<T> type,
            SerializationInputStream in)
    throws IOException, SerializationException {
        /* ABANDON HOPE, ALL YE WHO ENTER HERE... */
        T obj = null;
        try {
            Constructor<T> constructor =
                type.getConstructor(SerializationInputStream.class);
            obj = constructor.newInstance(in);
        } catch (Exception e) {
            /* We compress the myriad of possible exceptions that could occur
             * here down to a single exception (SerializationException) to
             * simplify implementations.  However, if the current log level
             * permits, we also embed more information in the exception detail
             * message. */
            throw new SerializationException("Could not instantiate object "
                    + "for deserialization.", e);
        }

        return obj;
    }

    /**
     * Deserializes and instantiates a ByteSerializable class from a stream.
     * This method should only be used in cases where the type of the
     * ByteSerializable class is not known at compile time.
     *
     * @param type The type of object to create (deserialize).
     *             For example, Something.class.
     *
     * @param in SerializationInputStream containing a serialized instance of
     *           the object being loaded.
     */
    public static <T extends ByteSerializable> T deserializeFromStream(
            Class<T> type, SerializationInputStream in)
    throws IOException, SerializationException {
        return deserialize(type, in);
    }

    /**
     * Dumps a ByteSerializable object to a portable byte array and stores it on
     * disk.
     *
     * @param obj The ByteSerializable object to serialize.
     * @param file File to write the ByteSerializable object to.
     */
    public static void persist(ByteSerializable obj, File file)
    throws IOException {
        FileOutputStream fOs = new FileOutputStream(file);
        BufferedOutputStream bOs = new BufferedOutputStream(fOs);
        SerializationOutputStream sOs = new SerializationOutputStream(bOs);
        sOs.writeSerializable(obj);
        sOs.close();
    }

    /**
     * Dumps a ByteSerializable object to a portable byte array and stores it on
     * disk.
     *
     * @param obj The ByteSerializable object to serialize.
     * @param fileName path the object should be written to.
     */
    public static void persist(ByteSerializable obj, String fileName)
    throws IOException {
        persist(obj, new File(fileName));
    }

    /**
     * Loads a ByteSerializable object's binary form from disk and
     * then instantiates a new object using the SerializationInputStream
     * constructor.
     *
     * @param type The type of object to create (deserialize).
     *             For example, Something.class.
     *
     * @param inFile File containing a serialized instance of the object being
     *               loaded.
     */
    public static <T extends ByteSerializable> T restore(Class<T> type,
            File inFile)
    throws IOException, SerializationException {
        FileInputStream fIn = new FileInputStream(inFile);
        BufferedInputStream bIn = new BufferedInputStream(fIn);
        SerializationInputStream sIn = new SerializationInputStream(bIn);
        T obj = deserializeFromStream(type, sIn);
        sIn.close();

        return obj;
    }

    /**
     * Loads a ByteSerializable object's binary form from disk and
     * then instantiates a new object using the SerializationInputStream
     * constructor.
     *
     * @param type The type of object to create (deserialize).
     *             For example, Something.class.
     *
     * @param fileName path the object should be read from.
     */
    public static <T extends ByteSerializable> T restore(Class<T> type,
            String fileName)
    throws IOException, SerializationException {
        File inFile = new File(fileName);
        return restore(type, inFile);
    }
}
