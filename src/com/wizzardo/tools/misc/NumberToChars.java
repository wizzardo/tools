package com.wizzardo.tools.misc;

/**
 * @author: wizzardo
 * Date: 12.11.14
 */
public class NumberToChars {
    final static int DELIMITER = 1000000000;
    final static long DELIMITER_LONG = 1000000000000000000l;

    public static int stringSizeOfWithMinus(long l) {
        if (l < 0) {
            if (l == Long.MIN_VALUE)
                return 20;

            return stringSizeOf(-l) + 1;
        }
        return stringSizeOf(l);
    }

    public static int stringSizeOf(long l) {
        if (l >= DELIMITER) {
            if (l >= DELIMITER_LONG)
                return 19;
            return 9 + stringSizeOf((int) (l / DELIMITER));
        } else
            return stringSizeOf((int) l);
    }

    public static int stringSizeOfWithMinus(int i) {
        if (i < 0) {
            if (i == Integer.MIN_VALUE)
                return 11;
            return stringSizeOf(-i) + 1;
        }
        return stringSizeOf(i);
    }

    public static int stringSizeOf(int i) {
        if (i >= 10000) {
            if (i >= 10000000) {
                if (i >= 1000000000)
                    return 10;
                else if (i >= 100000000)
                    return 9;
                else
                    return 8;
            } else {
                if (i >= 1000000)
                    return 7;
                else if (i >= 100000)
                    return 6;
                else
                    return 5;
            }
        } else {
            if (i >= 100) {
                if (i >= 1000)
                    return 4;
                else
                    return 3;
            } else {
                if (i >= 10)
                    return 2;
                else
                    return 1;
            }
        }
    }
}
