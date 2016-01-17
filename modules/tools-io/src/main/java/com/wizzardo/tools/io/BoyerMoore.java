package com.wizzardo.tools.io;

import java.nio.charset.Charset;
import java.util.Arrays;

public class BoyerMoore {

    protected static final Charset UTF8 = Charset.forName("utf-8");
    protected static final int ALPHABET_SIZE = 256;
    private final int[] charTable;
    private final int[] offsetTable;
    private final byte[] needle;

    public BoyerMoore(String s) {
        this(s.getBytes(UTF8));
    }

    public BoyerMoore(byte[] needle) {
        this(needle, 0, needle.length);
    }

    public BoyerMoore(byte[] needle, int offset, int length) {
        if (needle == null || length == 0)
            throw new IllegalArgumentException();

        this.needle = Arrays.copyOfRange(needle, offset, offset + length);
        charTable = makeCharTable(needle);
        offsetTable = makeOffsetTable(needle);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring. If it is not a substring, return -1.
     *
     * @param haystack The string to be scanned
     * @return The start index of the substring
     */
    public int search(byte[] haystack) {
        return search(haystack, 0, haystack.length);
    }

    public int search(byte[] haystack, int offset, int length) {
        byte[] needle = this.needle;
        int needleLength = needle.length - 1;
        int limit = offset + length;
        for (int i = needleLength + offset, j; i < limit; ) {
            for (j = needleLength; needle[j] == haystack[i]; --i, --j) {
                if (j == 0) {
                    return i;
                }
            }
            i += Math.max(offsetTable[needleLength - j], charTable[haystack[i] & 0xff]);
        }
        return -1;
    }

    /**
     * Makes the jump table based on the mismatched character information.
     */
    private static int[] makeCharTable(byte[] needle) {
        int[] table = new int[ALPHABET_SIZE];
        int l = needle.length;
        for (int i = 0; i < ALPHABET_SIZE; ++i) {
            table[i] = l;
        }

        l--;
        for (int i = 0; i < l; ++i) {
            table[needle[i] & 0xff] = l - i;
        }
        return table;
    }

    /**
     * Makes the jump table based on the scan offset which mismatch occurs.
     */
    private static int[] makeOffsetTable(byte[] needle) {
        int l = needle.length;
        int[] table = new int[l];
        int lastPrefixPosition = l;
        for (int i = l - 1; i >= 0; --i) {
            if (isPrefix(needle, i + 1)) {
                lastPrefixPosition = i + 1;
            }
            table[l - 1 - i] = lastPrefixPosition - i + l - 1;
        }
        for (int i = 0; i < l - 1; ++i) {
            int slen = suffixLength(needle, i);
            table[slen] = l - 1 - i + slen;
        }
        return table;
    }

    /**
     * Is needle[p:end] a prefix of needle?
     */
    private static boolean isPrefix(byte[] needle, int p) {
        int l = needle.length;
        for (int i = p, j = 0; i < l; ++i, ++j) {
            if (needle[i] != needle[j]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the maximum length of the substring ends at p and is a suffix.
     */
    private static int suffixLength(byte[] needle, int p) {
        int len = 0;
        for (int i = p, j = needle.length - 1; i >= 0 && needle[i] == needle[j]; --i, --j) {
            len++;
        }
        return len;
    }
}