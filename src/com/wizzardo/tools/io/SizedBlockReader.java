package com.wizzardo.tools.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Moxa
 */
public class SizedBlockReader {

    private InputStream in;
    private long blockLength = 0;
    private long readed = 0;

    public SizedBlockReader(InputStream in) {
        this.in = in;
    }

    public boolean hasNext() throws IOException {
        if (readed != blockLength) {
            throw new IllegalStateException("not all data has been read. " + readed + " != " + blockLength);
        }
        int r = 0, t = 0;
        byte[] b = new byte[8];
        while (t != 8 && (r = in.read(b, t, 8 - t)) != -1) {
            t += r;
        }
        if (t == 8) {
            blockLength = BytesTools.toLong(b);
            readed = 0;
            return true;
        }
        return false;
    }

    public long getBlockLength() {
        return blockLength;
    }

    public long lenght() {
        return blockLength;
    }

    public int read(byte[] b, int offset, int l) throws IOException {
        if (blockLength == readed) {
            return -1;
        }
        if (blockLength - readed < l) {
            l = (int) (blockLength - readed);
        }
        int r = in.read(b, offset, l);
        if (r == -1) {
            return -1;
        }
        readed += r;
        return r;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public void close() throws IOException {
        in.close();
    }
}
