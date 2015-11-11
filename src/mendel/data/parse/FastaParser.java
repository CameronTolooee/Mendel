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

import mendel.vptree.types.ProteinSequence;
import mendel.vptree.types.Sequence;

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
    private String fileName;

    /**
     * Constructs a FASTA file parser over the {@link java.io.InputStream}.
     *
     * @param in the input stream over the fasta file to parse
     */
    public FastaParser(InputStream in, String fileName) {
        reader = new BufferedReader(new InputStreamReader(in));
        this.fileName = fileName;
    }

    /**
     * Constructs a FASTA file parser over the {@link java.io.File}.
     *
     * @param file the fasta file to parse
     * @throws FileNotFoundException if the file does not exist, is a
     *                               directory rather than a regular file, or
     *                               for some other reason cannot
     *                               be opened for reading.
     */
    public FastaParser(File file) throws FileNotFoundException {
        this(new BufferedInputStream(new FileInputStream(file)), file.getName());
    }

    /**
     * Constructs a FASTA file parser over the FASTA file at the given
     * absolute path.
     *
     * @param absolutePath the absolute path to the FASTA file
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
        return new FastaRecordIterator(reader);
    }

    public Iterator<ProteinSequence> windowIterator() {
        return new FastaWindowIterator(reader);
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
     * <p/>
     *
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
                do {
                    line = br.readLine();
                    if (line == null && br != null) {
                        br.close();
                        break;
                    }
                } while (line.startsWith(">") || line.equals(""));
            } catch (IOException e) {
                e.printStackTrace();
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

    private class FastaWindowIterator implements Iterator<ProteinSequence> {
        private BufferedReader br;
        private ProteinSequence line;
        private int windowSize, position;
        private FastaRecordIterator recordIterator;
        private FastaRecord currentRecord;

        public FastaWindowIterator(BufferedReader br) {
            this(br, 100);
        }


        public FastaWindowIterator(BufferedReader br, int windowSize) {
            this.br = br;
            this.windowSize = windowSize;
            if (br == null) {
                throw new NullPointerException();
            }
            this.recordIterator = new FastaRecordIterator(br);
            if (recordIterator.hasNext()) {
                currentRecord = recordIterator.next();
            } else {
                throw new NullPointerException("Invalid fasta format");
            }
            this.position = 0;
            advance();
        }


        @Override
        public boolean hasNext() {
            return line != null;
        }

        @Override
        public ProteinSequence next() {
            ProteinSequence result = this.line;
            advance();
            return result;
        }

        private void advance() {
            if ((position + windowSize+1) > currentRecord.getSequence().length()) {
                if (recordIterator.hasNext()) {
                    currentRecord = recordIterator.next();
                } else {
                    line = null;
                    return;
                }
            }
            line = new ProteinSequence(currentRecord.getSequence().substring(position, position + windowSize));
            line.setSequenceLength(currentRecord.length());
            line.setSequenceID(currentRecord.toString());
            line.setSequencePos(position++);
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
        private BufferedReader br;
        private String line;
        private FastaRecord record;
        private boolean finished = false;

        public FastaRecordIterator(BufferedReader br) {
            this.br = br;
            record = new FastaRecord(fileName);
            if (br == null) {
                throw new NullPointerException();
            }
            advance();
        }

        @Override
        public boolean hasNext() {
            return record != null;
        }

        @Override
        public FastaRecord next() {
            FastaRecord result = this.record;
            record = null;
            if (!finished) {
                advance();
            }
            return result;
        }

        private void advance() {
            try {
                do {
                    line = br.readLine();
                    if (line == null && br != null) {
                        br.close();
                        return;
                    }
                } while (!line.startsWith(">"));

                record = new FastaRecord(fileName);
                record.setContigName(line);

                line = br.readLine();
                do {
                    record.appendSequence(line);
                    line = br.readLine();
                    if (line == null && br != null) {
                        br.close();
                        finished = true;
                        break;
                    }
                } while (!line.equals("") || !line.startsWith(">"));
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        private String fileName;
        private String contigName;
        private String sequence;

        public FastaRecord(String fileName) {
            this.fileName = fileName;
            this.contigName= "";
            this.sequence = "";
        }

        public FastaRecord(FastaRecord record) {
            this.fileName = record.getFileName();
            this.contigName = record.getContigName();
            this.sequence = record.getSequence();
        }

        public int length() {
            return sequence.length();
        }

        public void setContigName(String name) {
            this.contigName = name;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public String getContigName() {
            return contigName;
        }

        public void appendSequence(String seq) {
            this.sequence += seq;
        }

        public String getSequence() {
            return sequence;
        }

        public String toString() {
            return fileName + ":" + contigName;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
