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

package mendel.query;

import mendel.serialize.ByteSerializable;
import mendel.serialize.SerializationInputStream;
import mendel.serialize.SerializationOutputStream;
import mendel.vptree.types.Sequence;

import java.io.IOException;

public class QueryResult implements ByteSerializable {

    private Sequence query, value;
    private double identityScore, consecScore;

    public QueryResult(Sequence query, Sequence value) {
        this.query = query;
        this.value = value;
    }

    public double getIdentityScore() {
        return identityScore;
    }

    public void setIdentityScore(double identityScore) {
        this.identityScore = identityScore;
    }

    public double getConsecScore() {
        return consecScore;
    }

    public void setConsecScore(double consecScore) {
        this.consecScore = consecScore;
    }

    public Sequence getValue() {
        return value;
    }

    public Sequence getQuery() {
        return query;
    }

    @Deserialize
    public QueryResult(SerializationInputStream in) throws IOException {
        query = new Sequence(in);
        value = new Sequence(in);
        identityScore = in.readDouble();
        consecScore = in.readDouble();
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeSerializable(query);
        out.writeSerializable(value);
        out.writeDouble(identityScore);
        out.writeDouble(consecScore);
    }

    public String toString() {
        return value.toString();
    }
}
