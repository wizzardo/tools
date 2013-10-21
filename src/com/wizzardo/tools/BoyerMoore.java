/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools;

public class BoyerMoore {

    /**
     * Byte array, beginning at index 1 (for algorithmic convenience),
     * that contains the intended search pattern data.
     */
    private byte[] P;
    /**
     * The length of the search pattern.
     */
    private int m;
    /**
     * Table of jump distances for each mismatched character in the
     * alphabet for a given search pattern.  Must be recomputed for
     * each new pattern.
     */
    private int[] charJump;
    /**
     * Table of partial suffix match jump distances for a given pattern.
     * Must be recomputed for each new pattern.
     */
    private int[] matchJump;

    /**
     * Creates a precomputed Boyer-Moore byte string search object
     * from the given pattern.  The unicode characters in <code>pattern</code>
     * are truncated if greater than 255, and converted in twos-complement
     * fashion, to appropriate negative byte values, if necessary.
     * This method is provided as a convenience for searching for patterns
     * within 8 bit byte strings composed of character data.
     *
     * @param pattern The pattern create this object for.
     */
    public BoyerMoore(String pattern) {
        genPatternFromCharArray(pattern.toCharArray());
        computeJumps();
        computeMatchJumps();
    }

    /**
     * Creates a precomputed Boyer-Moore byte string search object
     * from the given pattern.
     *
     * @param pattern Binary pattern to search for.
     */
    public BoyerMoore(byte[] pattern) {
        genPatternFromByteArray(pattern, 0, pattern.length);
        computeJumps();
        computeMatchJumps();
    }

    /**
     * Creates a precomputed Boyer-Moore byte string search object
     * from a portion of the given pattern array.
     *
     * @param pattern Byte array containing a pattern to search for.
     * @param offset  Offset to beginning of search pattern.
     * @param length  Length of the search pattern.
     */
    public BoyerMoore(byte[] pattern, int offset, int length) {
        genPatternFromByteArray(pattern, offset, length);
        computeJumps();
        computeMatchJumps();
    }

    /**
     * Compares two integers and returns the lesser value.
     *
     * @param i1 First integer to compare.
     * @param i2 Second integer to compare.
     * @return The lesser of <code>i1</code> or <code>i2</code>.
     */
    private static final int min(int i1, int i2) {
        return (i1 < i2) ? i1 : i2;
    }

    /**
     * Compares two integers and returns the greater value.
     *
     * @param i1 First integer to compare.
     * @param i2 Second integer to compare.
     * @return The greater of <code>i1</code> or <code>i2</code>.
     */
    private static final int max(int i1, int i2) {
        return (i1 > i2) ? i1 : i2;
    }

    /**
     * Generates the pattern byte string <code>P</code> from a portion
     * of another byte string.
     *
     * @param bytes  The byte string from which to extract the pattern.
     * @param off    The array index within <code>bytes</code> from
     *               which to extract the pattern.
     * @param length The number of characters to extract from
     *               <code>bytes</code> into the pattern.
     */
    private final void genPatternFromByteArray(byte[] bytes, int off, int length) {
        int i, j;
        m = length;
// 31.03.2003. patch
//	P = new byte[length];
        P = new byte[length + 1];
        for (i = 1, j = off; i <= length; i++, j++) {
            P[i] = bytes[j];
        }
    }

    /**
     * Generates the pattern byte string <code>P</code> from a character
     * array.  The signed unicode characters are truncated to 8 bits, and
     * converted into signed byte values.  Characters between 128 and 255
     * are converted to their signed negative counterpart in
     * twos-complement fashion by subtracting 256.
     *
     * @param chars Unsigned unicode character array to turn into
     *              a signed byte array.
     */
    private final void genPatternFromCharArray(char[] chars) {
        m = chars.length;
        P = new byte[m + 1];
        for (int i = 1; i <= m; i++) {
            if (chars[i - 1] > 127) {
                P[i] = (byte) ((chars[i - 1] - 256) & 0xff);
            } else {
                P[i] = (byte) (chars[i - 1] & 0xff);
            }
        }
    }

    /**
     * Initializes the per-character jump table <code>charJump</code>
     * as specified by the Boyer-Moore algorithm.
     */
    private final void computeJumps() {
        charJump = new int[256];
        for (int i = 0; i < 255; i++) {
            charJump[i] = m;
        }
        for (int k = 1; k <= m; k++) {
            charJump[P[k] + 128] = m - k;
        }
    }

    /**
     * Computes a partial-match jump table that skips over
     * partially matching suffixes.
     */
    private void computeMatchJumps() {
        int k, q, qq, mm;
        int[] back = new int[m + 2];

        matchJump = new int[m + 2];
        mm = 2 * m;

        for (k = 1; k <= m; k++) {
            matchJump[k] = mm - k;
        }
        k = m;
        q = m + 1;
        while (k > 0) {
            back[k] = q;
            while ((q <= m) && (P[k] != P[q])) {
                matchJump[q] = min(matchJump[q], m - k);
                q = back[q];
            }
            k = k - 1;
            q = q - 1;
        }
        for (k = 1; k <= q; k++) {
            matchJump[k] = min(matchJump[k], m + q - k);
        }
        qq = back[q];
        while (q <= m) {
            while (q <= qq) {
                matchJump[q] = min(matchJump[q], qq - q + m);
                q = q + 1;
            }
            qq = back[qq];
        }
    }

    /**
     * Returns the length of the pattern for this searcher.
     *
     * @return The search pattern length.
     */
    public int getPatternLength() {
        return (m);
    }

    /**
     * Search for the previously pre-compiled pattern string in an
     * array of bytes.  This method uses the Boyer-Moore pattern
     * search algorithm.
     *
     * @param byteString Array of bytes in which to search
     *                   for the pattern.
     * @return The array index where the pattern
     * begins in the string, or <code>-1</code>
     * if the pattern was not found.
     */
    public int search(byte[] byteString) {
        return (search(byteString, 0, byteString.length));
    }

    /**
     * Search for the previously pre-compiled pattern string in an
     * array of bytes.  This method uses the Boyer-Moore pattern
     * search algorithm.
     *
     * @param byteString Array of bytes in which to search
     *                   for the pattern.
     * @param offset     The the index in <code>byteString</code>
     *                   where the search is to begin.
     * @param length     The number of bytes to search in
     *                   <code>byteString</code>.
     * @return The array index where the pattern
     * begins in the string, or <code>-1</code>
     * if the pattern was not found.
     */
    public int search(byte[] byteString, int offset, int length) {
        int j, k, len;
        j = m + offset;
        k = m;
        byte b;
        len = min(byteString.length, offset + length);
        while ((j <= len) && (k > 0)) {
            if ((b = byteString[j - 1]) == P[k]) {
                j = j - 1;
                k = k - 1;
            } else {
                j = j + max(charJump[b + 128], matchJump[k]);
                k = m;
            }
        }
        if (k == 0) {
            return (j);
        }
        return (-1); // No match.
    }
}