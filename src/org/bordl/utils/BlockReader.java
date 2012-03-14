/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 *
 * @author Moxa
 */
public class BlockReader {

    private InputStream in;
    private byte[] buffer, dynamicBuffer;
    private boolean close = false;
    private byte[] separator;
    private boolean wait = true;
    private int findedIndex = -1;
    private int offset = 0;
    private int r, endimg, buffered;

    public BlockReader(InputStream in, byte[] separator) {
        this.separator = Arrays.copyOf(separator, separator.length);
        this.in = in;
        buffer = new byte[separator.length];
    }

    public boolean hashNext() throws IOException {
        return dynamicBuffer != null || ready();
    }

    public void next() {
        wait = false;
    }

    public int read(byte[] b) throws IOException {
        if (b.length < separator.length) {
            throw new IllegalArgumentException("byte array MUST be bigger then separator");
        }
        if (wait) {
            return -1;
        }
        if (dynamicBuffer != null) {
            int k = dynamicBuffer.length - offset;
            if (k >= b.length) {
                System.arraycopy(dynamicBuffer, offset, b, 0, b.length);
                offset += b.length;
                if (offset == dynamicBuffer.length) {
                    dynamicBuffer = null;
                    offset = 0;
                }
                r = b.length;
            } else {
                System.arraycopy(dynamicBuffer, offset, b, 0, k);
                dynamicBuffer = null;
                offset = 0;
                r = k;
            }
        } else {
            if (buffered > 0) {
                System.arraycopy(buffer, 0, b, 0, buffered);
                r = in.read(b, buffered, b.length - buffered);
                if (r != -1) {
                    r += buffered;
                } else {
                    r = buffered;
                }
                buffered = 0;
            } else {
                r = in.read(b);
            }
        }
        if (r != -1) {
            findedIndex = endOfBlock(b, 0, r);
            if (findedIndex != -1) {
                wait = true;
                int length = r - findedIndex - separator.length;
                if (length != 0) {
                    dynamicBuffer = new byte[length];
                    System.arraycopy(b, findedIndex + separator.length, dynamicBuffer, 0, dynamicBuffer.length);
                }
                return findedIndex;
            } else {
                endimg = isEnding(b, separator);
                if (endimg != -1) {
                    buffered = b.length - endimg;
                    System.arraycopy(b, endimg, buffer, 0, buffered);
                    return endimg;
                } else {
                    return r;
                }
            }
        }
        return -1;
    }

//    public String readLine(String encoding) throws IOException {
//        String s = null;
//        while ((r = in.read(buffer)) != -1) {
//            int sl = -1;
//            if ((sl = endOfLine(buffer, r)) != -1) {
//                s = new String(buffer, 0, sl, encoding);
//                byte[] bb = new byte[10240];
//                System.arraycopy(buffer, 0, bb, 0, sl);
//                buffer = bb;
//                r = r - sl;
//                break;
//            }
//        }
//
//        if (s != null) {
//            s = s.trim();
//        }
//        return s;
//    }
    public void close() throws IOException {
        in.close();
        close = true;
        in = null;
    }

    public static int isEnding(byte[] b, byte[] endsWith) {
        int i = b.length - endsWith.length;
        if (i < 0) {
            i = 0;
        }
        outer:
        for (; i < b.length; i++) {
            int j = 0;
            while (j < endsWith.length && j + i < b.length && b[i + j] == endsWith[j]) {
                j++;
            }
            if (j + i == b.length) {
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

//    private int endOfLine(byte[] b, int length) {
//        int i = 0;
//        while (i < length) {
//            if (b[i] == 13 || b[i] == 10) {
//                return i;
//            }
//            i++;
//        }
//        return -1;
//    }
    public boolean ready() throws IOException {
        if (!close) {
            return in.available() > 0;
        } else {
            return false;
        }
    }
}