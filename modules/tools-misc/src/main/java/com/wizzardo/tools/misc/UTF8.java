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

        while (off < limit && chars[off] < 128) {
            bytes[l++] = (byte) chars[off++];
        }

        while (off < limit) {
            char ch = chars[off++];
            if (ch < 128) {
                bytes[l++] = (byte) ch;
            } else if (ch < 2048) {
                bytes[l++] = (byte) (192 | ch >> 6);
                bytes[l++] = (byte) (128 | ch & 63);
            } else if (ch >= '\uD800' && ch < '\uE000') {//surrogate
                int r = off < limit ? parseSurrogate(ch, chars[off]) : -1;
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
                bytes[l++] = (byte) (224 | ch >> 12);
                bytes[l++] = (byte) (128 | ch >> 6 & 63);
                bytes[l++] = (byte) (128 | ch & 63);
            }
        }

        return l;
    }

    private static int parseSurrogate(char ch, char ch2) {
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
