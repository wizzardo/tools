package com.wizzardo.tools.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: moxa
 * Date: 11/16/12
 */
public class BytesTools {

    public static byte[] toBytes(long l) {
        return toBytes(l, 8);
    }

    public static void toBytes(long l, OutputStream out) throws IOException {
        toBytes(l, out, 8);
    }

    public static long toLong(byte[] b) {
        return toLong(b, 0);
    }

    public static long toLong(byte[] b, int offset) {
        return toNumber(b, offset, 8);
    }

    public static byte[] toBytes(int i) {
        return toBytes(i, 4);
    }

    public static void toBytes(int i, OutputStream out) throws IOException {
        toBytes(i, out, 4);
    }

    public static int toInt(byte[] b) {
        return toInt(b, 0);
    }

    public static int toInt(byte[] b, int offset) {
        return (int) toNumber(b, offset, 4);
    }

    public static long toNumber(byte[] b, int offset, int bytesCount) {
        if (b.length - offset < bytesCount)
            throw new IllegalArgumentException("length of array must by " + bytesCount + " bytes, but get " + (b.length - offset));

        int k = (bytesCount - 1) * 8;
        long l = 0;
        for (int i = 0; i < bytesCount; i++) {
            l |= ((long) b[offset + i] & 0xff) << k;
            k -= 8;
        }
        return l;
    }

    public static void toBytes(long l, OutputStream out, int bytesCount) throws IOException {
        int k = (bytesCount - 1) * 8;
        for (int i = 0; i < bytesCount; i++) {
            out.write((byte) (l >> k));
            k -= 8;
        }
    }

    public static byte[] toBytes(long l, int bytesCount) {
        int k = (bytesCount - 1) * 8;
        byte[] bytes = new byte[bytesCount];
        for (int i = 0; i < bytesCount; i++) {
            bytes[i] = (byte) (l >> k);
            k -= 8;
        }
        return bytes;
    }

}
