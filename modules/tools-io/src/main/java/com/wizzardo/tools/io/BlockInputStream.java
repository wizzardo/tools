package com.wizzardo.tools.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Moxa
 */
public class BlockInputStream extends InputStream {

    private PushbackInputStream in;
    private byte[] buffer, dynamicBuffer;
    private boolean close = false;
    private byte[] separator;
    private boolean wait = true;
    private int findedIndex = -1;
    private int offset = 0;
    private int r, endimg, buffered;
    private BoyerMoore bm;
    private long blockLength = 0;
    private long limit = 0;
    private long totalRead = 0;
    private ProgressListener progressListener;

    public BlockInputStream(InputStream in, byte[] separator) {
        this.separator = Arrays.copyOf(separator, separator.length);
        this.in = new PushbackInputStream(in, 1024 * 50);
        buffer = new byte[separator.length];
        bm = new BoyerMoore(separator);
    }

    public BlockInputStream(InputStream in, byte[] separator, long limit) {
        this(in, separator);
        this.limit = limit;
    }

    public BlockInputStream(InputStream in, byte[] separator, long limit, ProgressListener progressListener) {
        this(in, separator, limit);
        this.progressListener = progressListener;
    }

    public InputStream getInputStream() throws IOException {
        if (!wait) {
            throw new IllegalStateException("this method can be executed only between blocks");
        }
        if (dynamicBuffer != null) {
            in.unread(dynamicBuffer, offset, dynamicBuffer.length - offset);
        }
        return in;
    }

    public boolean hasNext() throws IOException {
        if (limitReached()) {
            return false;
        }
        return dynamicBuffer != null || ready();
    }

    public void next() {
        wait = false;
        blockLength = 0;
    }

    @Override
    public int read() throws IOException {
        if (limitReached()) {
            return -1;
        }

        byte[] bytes = new byte[1];
        int r = read(bytes);
        if (r == 0)
            return -1;

        return bytes[0] & 0xff;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    private boolean limitReached() {
        return dynamicBuffer == null && limit > 0 && totalRead >= limit;
    }

    public int read(byte[] b, int off, int l) throws IOException {
        if (limitReached()) {
            return -1;
        }
        if (l < separator.length) {
            throw new IllegalArgumentException("byte array MUST be bigger then separator");
        }
        if (wait) {
            return -1;
        }
        if (dynamicBuffer != null) {
            int k = dynamicBuffer.length - offset;
            if (k >= l) {
                System.arraycopy(dynamicBuffer, offset, b, off, l);
                offset += l;
                if (offset == dynamicBuffer.length) {
                    dynamicBuffer = null;
                    offset = 0;
                }
                r = l;
            } else {
                System.arraycopy(dynamicBuffer, offset, b, off, k);
                dynamicBuffer = null;
                offset = 0;
                r = k;
            }
        } else {
            if (buffered > 0) {
                System.arraycopy(buffer, 0, b, off, buffered);
                r = in.read(b, buffered + off, l - buffered);
                if (r != -1) {
                    r += buffered;
                    totalRead += r;
                } else {
                    r = buffered;
                }
                buffered = 0;
            } else {
                if (ready()) {
                    r = in.read(b, off, l);
                    totalRead += r;
                } else {
                    r = -1;
                }
            }
        }

        if (progressListener != null) {
            if (totalRead != limit)
                progressListener.setProgress((int) (totalRead * 100f / limit));
            else
                progressListener.setProgress(100);
        }

        if (r != -1) {
            findedIndex = bm.search(b, off, r);
            if (findedIndex != -1) {
                wait = true;
                int length = r - findedIndex - separator.length + off;
                if (length != 0) {
                    dynamicBuffer = new byte[length];
                    System.arraycopy(b, findedIndex + separator.length, dynamicBuffer, 0, dynamicBuffer.length);
                }
                blockLength += findedIndex;
                return findedIndex - off;
            } else {
                endimg = isEnding(b, r, separator);
                if (endimg != -1) {
                    buffered = r - endimg;
                    System.arraycopy(b, endimg, buffer, 0, buffered);
                    blockLength += endimg;
                    return endimg;
                } else {
                    blockLength += r;
                    return r;
                }
            }
        }
        return -1;
    }

    public long getBlockLength() {
        return blockLength;
    }

    public void close() throws IOException {
        in.close();
        close = true;
        in = null;
    }

    public static int isEnding(byte[] b, int length, byte[] endsWith) {
        int i = length - endsWith.length;
        if (i < 0) {
            i = 0;
        }
        outer:
        for (; i < length; i++) {
            int j = 0;
            while (j < endsWith.length && j + i < length && b[i + j] == endsWith[j]) {
                j++;
            }
            if (j + i == length) {
                return i;
            }
        }
        return -1;
    }

    private int endOfBlock(byte[] b, int offset, int length) {
        int i = offset;
        if (i < 0) {
            i = 0;
        }
        outer:
        while (i <= length - separator.length) {
            int j = 0;
            while (j < separator.length && b[i + j] == separator[j]) {
                j++;
            }
            if (j == separator.length) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public boolean ready() throws IOException {
        if (!close && (dynamicBuffer == null || limit > 0 && totalRead < limit)) {
            long wait = 0;
            int r;
            while ((r = in.read()) == -1 && wait < 500) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BlockInputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
                wait++;
            }
            if (r == -1) {
                return false;
            }
            in.unread(r);
            return true;
        } else {
            return false;
        }
    }
}
