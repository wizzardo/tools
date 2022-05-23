package com.wizzardo.tools.http;

import com.wizzardo.tools.io.IOTools;

import java.io.*;

public interface Body {
    long length();

    void write(OutputStream out) throws IOException;

    InputStream getInputStream() throws IOException;

    class ByteArrayBody implements Body {
        final byte[] bytes;
        final int offset;
        final int length;

        public ByteArrayBody(byte[] bytes, int offset, int length) {
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        }

        public ByteArrayBody(byte[] bytes) {
            this(bytes, 0, bytes.length);
        }


        @Override
        public long length() {
            return length;
        }

        @Override
        public void write(OutputStream out) throws IOException {
            out.write(bytes, offset, length);
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes, 0, length);
        }
    }

    class FileBody implements Body {
        final File file;

        public FileBody(File file) {
            this.file = file;
        }


        @Override
        public long length() {
            return file.length();
        }

        @Override
        public void write(OutputStream out) throws IOException {
            try (
                    InputStream in = getInputStream()
            ) {
                IOTools.copy(in, out);
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }
    }
}
