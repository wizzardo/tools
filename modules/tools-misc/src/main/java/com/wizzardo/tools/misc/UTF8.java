package com.wizzardo.tools.misc;

import java.util.Arrays;

/**
 * Created by wizzardo on 30.03.15.
 */
public class UTF8 {

    protected byte[] buffer;

    public byte[] toBytes(char[] chars, int offset, int length) {
        if (buffer == null || buffer.length < length * 4)
            buffer = new byte[length * 4];

        return encodeAndTrim(chars, offset, length, buffer);
    }

    public byte[] toBytes(char[] chars) {
        return toBytes(chars, 0, chars.length);
    }

    public static byte[] encode(char[] chars) {
        return encode(chars, 0, chars.length);
    }

    public static byte[] encode(char[] chars, int off, int length) {
        byte[] bytes = new byte[length * 4];
        int l = encode(chars, off, length, bytes);
        return Arrays.copyOf(bytes, l);
    }

    public static byte[] encodeAndTrim(char[] chars, int off, int length, byte[] bytes) {
        int l = encode(chars, off, length, bytes);
        return Arrays.copyOf(bytes, l);
    }

    public static int encode(char[] chars, int off, int length, byte[] bytes) {
        int limit = off + length;
        int l = 0;

        int ch;
        while (off < limit) {
            if ((ch = chars[off++]) < 128)
                bytes[l++] = (byte) ch;
            else {
                off--;
                break;
            }
        }

        while (off < limit) {
            int c = chars[off++];
            if (c < 128) {
                bytes[l++] = (byte) c;
            } else if (c < 2048) {
                bytes[l++] = (byte) (192 | c >> 6);
                bytes[l++] = (byte) (128 | c & 63);
            } else if (c >= '\uD800' && c < '\uE000') {//surrogate
                int r = off < limit ? parseSurrogate(c, chars[off]) : -1;
                if (r < 0) {
                    bytes[l++] = '?';
                } else {
                    bytes[l++] = (byte) (240 | r >> 18);
                    bytes[l++] = (byte) (128 | r >> 12 & 63);
                    bytes[l++] = (byte) (128 | r >> 6 & 63);
                    bytes[l++] = (byte) (128 | r & 63);
                    ++off;
                }
            } else {
                bytes[l++] = (byte) (224 | c >> 12);
                bytes[l++] = (byte) (128 | c >> 6 & 63);
                bytes[l++] = (byte) (128 | c & 63);
            }
        }

        return l;
    }

    private static int parseSurrogate(int ch, int ch2) {
        if (ch >= '\uD800' && ch < '\uDC00') {
            if (ch2 >= '\uDC00' && ch2 < '\uE000')
                return ((ch & 1023) << 10 | ch2 & 1023) + 65536;
            else
                return -1;
        } else if (ch >= '\uDC00' && ch < '\uE000')
            return -1;
        else
            return ch;
    }
}
