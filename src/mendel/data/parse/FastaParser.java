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

package mendel.data.parse;

import java.io.*;
import java.util.Iterator;

/**
 * A parser for iterating over FASTA files. This is used for loading data
 * into the Mendel distrusted file system. A typical use case would be to
 * use the default line iterator (vie the {@link this.iterator()} method) and
 * staging each line as a data block. The iterator's remove method is not
 * supported. Modifying the lines or records will invalidate the iterator,
 * making its behavior undefined.
 *
 * @author ctolooee
 */
public class FastaParser implements Iterable<String> {

    private BufferedReader reader;

    /**
     * Constructs a FASTA file parser over the {@link java.io.InputStream}.
     *
     * @param in  the input stream over the fasta file to parse
     */
    public FastaParser(InputStream in) {
        reader = new BufferedReader(new InputStreamReader(in));
    }

    /**
     * Constructs a FASTA file parser over the {@link java.io.File}.
     *
     * @param file  the fasta file to parse
     * @throws FileNotFoundException if the file does not exist, is a
     *                               directory rather than a regular file, or
     *                               for some other reason cannot
     *                               be opened for reading.
     */
    public FastaParser(File file) throws FileNotFoundException {
        this(new BufferedInputStream(new FileInputStream(file)));
    }

    /**
     * Constructs a FASTA file parser over the FASTA file at the given
     * absolute path.
     *
     * @param absolutePath  the absolute path to the FASTA file
     * @throws FileNotFoundException if the file does not exist, is a
     *                               directory rather than a regular file, or
     *                               for some other reason cannot
     *                               be opened for reading.
     */
    public FastaParser(String absolutePath) throws FileNotFoundException {
        this(new File(absolutePath));
    }

    /**
     * Returns a {@link mendel.data.parse.FastaParser.FastaLineIterator} over
     * the FASTA file.
     *
     * @return the FastaLineIterator
     */
    public Iterator<String> lineIterator() {
        return iterator();
    }

    /**
     * Returns a {@link mendel.data.parse.FastaParser.FastaRecordIterator}
     * over the FASTA file.
     *
     * @return the FastaRecordIterator
     */
    public Iterator<FastaRecord> recordIterator() {
        return new FastaRecordIterator();
    }


    @Override
    public Iterator<String> iterator() {
        return new FastaLineIterator(reader);
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     * Once the stream has been closed, further iterator invocations will throw
     * an IOException. Closing a previously closed stream has no effect.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Implementation inspired by Brian Gilstrap's blog on making
     * BufferedReader's iterable
     * <p>
     * @see <a href="http://bit.ly/1AXaiG4" >Brian Gilstrap's blog post</a>
     */
    private class FastaLineIterator implements Iterator<String> {

        private BufferedReader br;
        private String line;

        public FastaLineIterator(BufferedReader br) {
            this.br = br;
            if (br == null) {
                throw new NullPointerException();
            }
            advance();
        }

        private void advance() {
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null && br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean hasNext() {
            return line != null;
        }

        @Override
        public String next() {
            String line = this.line;
            advance();
            return line;
        }

        /**
         * NOT SUPPORTED
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private class FastaRecordIterator implements Iterator<FastaRecord> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public FastaRecord next() {
            return null;
        }

        /**
         * NOT SUPPORTED
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static class FastaRecord {

        private String name;
        private String sequence;

        public FastaRecord() {
        }

        public FastaRecord(FastaRecord record) {
            this.name = record.getName();
            this.sequence = record.getSequence();
        }

        public void copy(FastaRecord record) {
            this.name = record.getName();
            this.sequence = record.getSequence();
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public String getName() {
            return name;
        }

        public String getSequence() {
            return sequence;
        }
    }
}
