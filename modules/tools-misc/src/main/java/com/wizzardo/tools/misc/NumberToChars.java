package com.wizzardo.tools.misc;

/**
 * @author: wizzardo
 * Date: 12.11.14
 */
public class NumberToChars {
    public final int MAX_INT_SIZE = 11;
    public final int MAX_LONG_SIZE = 20;

    final static int DELIMITER = 1000000000;
    final static long DELIMITER_LONG = 1000000000000000000l;
    final static long DELIMITER_LONG_2X = 2147483648000000000l;
    final static char[] THOUSAND;
    final static byte[] THOUSAND_BYTES;

    static {
        THOUSAND = new char[4000]; // because <<2 faster than *3
        for (int i = 0; i < 1000; i++) {
            THOUSAND[4 * i] = (char) ('0' + (i % 10));
            THOUSAND[4 * i + 1] = (char) ('0' + ((i / 10) % 10));
            THOUSAND[4 * i + 2] = (char) ('0' + (i / 100));
        }
        THOUSAND_BYTES = new byte[4000]; // because <<2 faster than *3
        for (int i = 0; i < 1000; i++) {
            THOUSAND_BYTES[4 * i] = (byte) ('0' + (i % 10));
            THOUSAND_BYTES[4 * i + 1] = (byte) ('0' + ((i / 10) % 10));
            THOUSAND_BYTES[4 * i + 2] = (byte) ('0' + (i / 100));
        }
    }

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

    public static int toChars(long l, char[] buf, int offset) {
        if (l < 0) {
            if (l == Long.MIN_VALUE) {
                writeMinLong(buf, offset);
                return offset + 20;
            }
            buf[offset++] = '-';
            l = -l;
        }

        if (l > DELIMITER) {
            if (l >= DELIMITER_LONG_2X) {
                int half = (int) (l / DELIMITER_LONG);
                getChars(half, offset + 1, buf);

                int center = (int) ((l - half * DELIMITER_LONG) / DELIMITER);
                getChars(center, offset + 10, buf);
                fillWithZeroes(buf, offset + 1, 9 - stringSizeOf(center));

                half = (int) (l - half * DELIMITER_LONG - center * DELIMITER);
                getChars(half, offset + 19, buf);
                fillWithZeroes(buf, offset + 10, 9 - stringSizeOf(half));
                return offset + 19;
            }

            int half = (int) (l / DELIMITER);
            offset += stringSizeOf(half);
            getChars(half, offset, buf);

            half = (int) (l - half * 1l * DELIMITER);
            getChars(half, offset + 9, buf);
            fillWithZeroes(buf, offset, 9 - stringSizeOf(half));
            return offset + 9;
        } else {
            int i = (int) l;
            offset += stringSizeOf(i);
            getChars(i, offset, buf);
            return offset;
        }
    }

    public static int toChars(long l, byte[] buf, int offset) {
        if (l < 0) {
            if (l == Long.MIN_VALUE) {
                writeMinLong(buf, offset);
                return offset + 20;
            }
            buf[offset++] = '-';
            l = -l;
        }

        if (l > DELIMITER) {
            if (l >= DELIMITER_LONG_2X) {
                int half = (int) (l / DELIMITER_LONG);
                getChars(half, offset + 1, buf);

                int center = (int) ((l - half * DELIMITER_LONG) / DELIMITER);
                getChars(center, offset + 10, buf);
                fillWithZeroes(buf, offset + 1, 9 - stringSizeOf(center));

                half = (int) (l - half * DELIMITER_LONG - center * DELIMITER);
                getChars(half, offset + 19, buf);
                fillWithZeroes(buf, offset + 10, 9 - stringSizeOf(half));
                return offset + 19;
            }

            int half = (int) (l / DELIMITER);
            offset += stringSizeOf(half);
            getChars(half, offset, buf);

            half = (int) (l - half * 1l * DELIMITER);
            getChars(half, offset + 9, buf);
            fillWithZeroes(buf, offset, 9 - stringSizeOf(half));
            return offset + 9;
        } else {
            int i = (int) l;
            offset += stringSizeOf(i);
            getChars(i, offset, buf);
            return offset;
        }
    }

    private static void fillWithZeroes(char[] buf, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            buf[i] = '0';
        }
    }

    private static void fillWithZeroes(byte[] buf, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            buf[i] = '0';
        }
    }

    /*
    * @return new offset in char buffer
    */
    public static int toChars(int i, char[] buf, int offset) {
        if (i < 0) {
            if (i == Integer.MIN_VALUE) {
                writeMinInteger(buf, offset);
                return offset + 11;
            }
            buf[offset++] = '-';
            i = -i;
        }

        offset += stringSizeOf(i);
        getChars(i, offset, buf);
        return offset;
    }

    /*
    * @return new offset in byte buffer
    */
    public static int toChars(int i, byte[] buf, int offset) {
        if (i < 0) {
            if (i == Integer.MIN_VALUE) {
                writeMinInteger(buf, offset);
                return offset + 11;
            }
            buf[offset++] = '-';
            i = -i;
        }

        offset += stringSizeOf(i);
        getChars(i, offset, buf);
        return offset;
    }

    private static void writeMinInteger(char[] buf, int offset) {
        //-2 147 483 648
        buf[offset++] = '-';
        buf[offset++] = '2';

        buf[offset++] = '1';
        buf[offset++] = '4';
        buf[offset++] = '7';

        buf[offset++] = '4';
        buf[offset++] = '8';
        buf[offset++] = '3';

        buf[offset++] = '6';
        buf[offset++] = '4';
        buf[offset] = '8';
    }

    private static void writeMinInteger(byte[] buf, int offset) {
        //-2 147 483 648
        buf[offset++] = '-';
        buf[offset++] = '2';

        buf[offset++] = '1';
        buf[offset++] = '4';
        buf[offset++] = '7';

        buf[offset++] = '4';
        buf[offset++] = '8';
        buf[offset++] = '3';

        buf[offset++] = '6';
        buf[offset++] = '4';
        buf[offset] = '8';
    }

    private static void writeMinLong(char[] buf, int offset) {
        //-9 223 372 036 854 775 808
        buf[offset++] = '-';
        buf[offset++] = '9';

        buf[offset++] = '2';
        buf[offset++] = '2';
        buf[offset++] = '3';

        buf[offset++] = '3';
        buf[offset++] = '7';
        buf[offset++] = '2';

        buf[offset++] = '0';
        buf[offset++] = '3';
        buf[offset++] = '6';

        buf[offset++] = '8';
        buf[offset++] = '5';
        buf[offset++] = '4';

        buf[offset++] = '7';
        buf[offset++] = '7';
        buf[offset++] = '5';

        buf[offset++] = '8';
        buf[offset++] = '0';
        buf[offset] = '8';
    }

    private static void writeMinLong(byte[] buf, int offset) {
        //-9 223 372 036 854 775 808
        buf[offset++] = '-';
        buf[offset++] = '9';

        buf[offset++] = '2';
        buf[offset++] = '2';
        buf[offset++] = '3';

        buf[offset++] = '3';
        buf[offset++] = '7';
        buf[offset++] = '2';

        buf[offset++] = '0';
        buf[offset++] = '3';
        buf[offset++] = '6';

        buf[offset++] = '8';
        buf[offset++] = '5';
        buf[offset++] = '4';

        buf[offset++] = '7';
        buf[offset++] = '7';
        buf[offset++] = '5';

        buf[offset++] = '8';
        buf[offset++] = '0';
        buf[offset] = '8';
    }

    private static void getChars(int i, int index, char[] buf) {
        int r;
        if (i >= 1000) {
            int q = i / 1000;
            r = i - (q * 1000);
            i = q;
            r = r << 2;
            buf[index - 1] = THOUSAND[r];
            buf[index - 2] = THOUSAND[r + 1];
            buf[index -= 3] = THOUSAND[r + 2];
            if (i >= 1000) {
                q = i / 1000;
                r = i - (q * 1000);
                i = q;
                r = r << 2;
                buf[index - 1] = THOUSAND[r];
                buf[index - 2] = THOUSAND[r + 1];
                buf[index -= 3] = THOUSAND[r + 2];
                if (i >= 1000) {
                    q = i / 1000;
                    r = i - (q * 1000);
                    i = q;
                    r = r << 2;
                    buf[index - 1] = THOUSAND[r];
                    buf[index - 2] = THOUSAND[r + 1];
                    buf[index -= 3] = THOUSAND[r + 2];
                }
            }
        }

        r = i << 2;
        buf[index - 1] = THOUSAND[r];
        if (i < 10)
            return;
        buf[index - 2] = THOUSAND[r + 1];
        if (i < 100)
            return;
        buf[index - 3] = THOUSAND[r + 2];
    }

    private static void getChars(int i, int index, byte[] buf) {
        int r;
        if (i >= 1000) {
            int q = i / 1000;
            r = i - (q * 1000);
            i = q;
            r = r << 2;
            buf[index - 1] = THOUSAND_BYTES[r];
            buf[index - 2] = THOUSAND_BYTES[r + 1];
            buf[index -= 3] = THOUSAND_BYTES[r + 2];
            if (i >= 1000) {
                q = i / 1000;
                r = i - (q * 1000);
                i = q;
                r = r << 2;
                buf[index - 1] = THOUSAND_BYTES[r];
                buf[index - 2] = THOUSAND_BYTES[r + 1];
                buf[index -= 3] = THOUSAND_BYTES[r + 2];
                if (i >= 1000) {
                    q = i / 1000;
                    r = i - (q * 1000);
                    i = q;
                    r = r << 2;
                    buf[index - 1] = THOUSAND_BYTES[r];
                    buf[index - 2] = THOUSAND_BYTES[r + 1];
                    buf[index -= 3] = THOUSAND_BYTES[r + 2];
                }
            }
        }

        r = i << 2;
        buf[index - 1] = THOUSAND_BYTES[r];
        if (i < 10)
            return;
        buf[index - 2] = THOUSAND_BYTES[r + 1];
        if (i < 100)
            return;
        buf[index - 3] = THOUSAND_BYTES[r + 2];
    }
}
