package org.bordl.utils.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: moxa
 * Date: 11/16/12
 */
class BytesUtils {

    public static byte[] toBytes(long l) {
        return new byte[]{
                (byte) (l >> 56),
                (byte) (l >> 48),
                (byte) (l >> 40),
                (byte) (l >> 32),
                (byte) (l >> 24),
                (byte) (l >> 16),
                (byte) (l >> 8),
                (byte) (l >> 0),
        };
    }

    public static void toBytes(long l, OutputStream out) throws IOException {
        out.write((byte) (l >> 56));
        out.write((byte) (l >> 48));
        out.write((byte) (l >> 40));
        out.write((byte) (l >> 32));
        out.write((byte) (l >> 24));
        out.write((byte) (l >> 16));
        out.write((byte) (l >> 8));
        out.write((byte) (l >> 0));
    }

    public static long toLong(byte[] b) {
        return toLong(b, 0);
    }

    public static long toLong(byte[] b, int offset) {
        return ((((long) b[offset] & 0xff) << 56) |
                (((long) b[offset + 1] & 0xff) << 48) |
                (((long) b[offset + 2] & 0xff) << 40) |
                (((long) b[offset + 3] & 0xff) << 32) |
                (((long) b[offset + 4] & 0xff) << 24) |
                (((long) b[offset + 5] & 0xff) << 16) |
                (((long) b[offset + 6] & 0xff) << 8) |
                (((long) b[offset + 7] & 0xff) << 0));
    }
}
