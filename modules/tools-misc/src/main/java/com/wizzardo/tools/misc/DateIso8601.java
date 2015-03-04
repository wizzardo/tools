package com.wizzardo.tools.misc;

import com.wizzardo.tools.reflection.StringReflection;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author: wizzardo
 * Date: 6/5/14
 */
public class DateIso8601 {

    private static final TimeZone Z = TimeZone.getTimeZone("GMT");
    private static final TimeZone LOCAL = TimeZone.getDefault();
    private static final int HOUR = 1000 * 60 * 60;
    private static final int MINUTE = 1000 * 60;

    /**
     * @param s should be in format YYYY-MM-DDTHH:mm:ss.sssZ
     */
    public static Date parse(String s) {
        //YYYY-MM-DDTHH:mm:ss.sssZ
        Calendar calendar = GregorianCalendar.getInstance();
        int length = s.length();
        char[] chars = StringReflection.chars(s);
        int i = length == chars.length ? 0 : StringReflection.offset(s);


        calendar.set(Calendar.YEAR, getInt4(chars, i));
        i = 4;
        char c = chars[i];
        if (c == '-') {
            i++;
            c = chars[i];
        }
        calendar.set(Calendar.MONTH, getInt2(chars, i, c) - 1);
        i += 2;

        c = chars[i];
        if (c == '-') {
            i++;
            c = chars[i];
        }

        calendar.set(Calendar.DAY_OF_MONTH, getInt2(chars, i, c));
        i += 2;

        if (i >= length) {
            calendar.setTimeZone(Z);
            clearTime(calendar);
            return calendar.getTime();
        }

        checkOr(chars[i], 'T', ' ');
        calendar.set(Calendar.HOUR_OF_DAY, getInt2(chars, i + 1));

        i += 3;

        if (i >= length) {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.setTimeZone(LOCAL);
            return calendar.getTime();
        }

        c = chars[i];
        if (c == ':') {
            i++;
            c = chars[i];
        }
        if (isInt(c)) {
            calendar.set(Calendar.MINUTE, getInt2(chars, i, c));
            i += 2;

            if (i >= length) {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.setTimeZone(LOCAL);
                return calendar.getTime();
            }

            c = chars[i];
            if (c == ':') {
                i++;
                c = chars[i];
            }

            if (isInt(c)) {
                calendar.set(Calendar.SECOND, getInt2(chars, i, c));
                i += 2;

                if (i >= length) {
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.setTimeZone(LOCAL);
                    return calendar.getTime();
                }

                c = chars[i];
                if (c == '.') {
                    i++;
                    c = chars[i];
                }
                if (isInt(c)) {
                    calendar.set(Calendar.MILLISECOND, getInt3(chars, i, c));
                    i += 3;
                } else
                    calendar.set(Calendar.MILLISECOND, 0);
            } else {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }
        } else {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }


        if (i == length) {
            calendar.setTimeZone(LOCAL);
            return calendar.getTime();
        }
        if (chars[i] == 'Z') {
            calendar.setTimeZone(Z);
            return calendar.getTime();
        }

        c = chars[i];
        boolean plus = c == '+';
        if (!plus)
            check(c, '-');

        int hours = getInt2(chars, i + 1);
        i += 3;
        if (i >= length) {
            calendar.setTimeZone(new SimpleTimeZone(hours * HOUR * (plus ? 1 : -1)));
            return calendar.getTime();
        }
        c = chars[i];
        if (c == ':') {
            i++;
            c = chars[i];
        }

        int minutes = getInt2(chars, i, c);
        calendar.setTimeZone(new SimpleTimeZone((int) (hours * HOUR + minutes * MINUTE) * (plus ? 1 : -1)));

        return calendar.getTime();
    }

    private static int getInt(char c) {
        if (!isInt(c))
            throw new IllegalArgumentException("char should be an int, but was: " + c);
        return c - 48;
    }

    private static boolean isInt(char c) {
        return c >= 48 && c <= 57;
    }

    private static void check(char c, char check) {
        if (c != check)
            throw new IllegalArgumentException("char should be an '" + check + "', but was: " + c);
    }

    private static void checkOr(char c, char check, char check2) {
        if (c != check && c != check2)
            throw new IllegalArgumentException("char should be an '" + check + "' or '" + check2 + "', but was: " + c);
    }

    private static int getInt2(char[] chars, int offset) {
        int i = getInt(chars[offset]);
        return i * 10 + getInt(chars[offset + 1]);
    }

    private static int getInt2(char[] chars, int offset, char first) {
        int i = getInt(first);
        return i * 10 + getInt(chars[offset + 1]);
    }

    private static int getInt3(char[] chars, int offset, char first) {
        int i = getInt(first);
        i = i * 10 + getInt(chars[offset + 1]);
        return i * 10 + getInt(chars[offset + 2]);
    }

    private static int getInt3(char[] chars, int offset) {
        int i = getInt(chars[offset]);
        i = i * 10 + getInt(chars[offset + 1]);
        return i * 10 + getInt(chars[offset + 2]);
    }

    private static int getInt4(char[] chars, int offset) {
        int i = getInt(chars[offset]);
        i = i * 10 + getInt(chars[offset + 1]);
        i = i * 10 + getInt(chars[offset + 2]);
        return i * 10 + getInt(chars[offset + 3]);
    }

    private static void clearTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.setTimeZone(Z);
    }

    public static String format(Date date) {
        return format(date, Z);
    }

    public static String format(Date date, TimeZone timeZone) {
        return StringReflection.createString(formatToChars(date, timeZone));
    }

    public static char[] formatToChars(Date date) {
        return formatToChars(date, Z);
    }

    public static char[] formatToChars(Date date, TimeZone timeZone) {
        Calendar calendar = GregorianCalendar.getInstance(timeZone);
        calendar.setTime(date);

        char[] chars;
        int offset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
        if (offset == 0)
            chars = new char[24]; // YYYY-MM-DDTHH:mm:ss.SSSZ
        else
            chars = new char[28]; // YYYY-MM-DDTHH:mm:ss.SSS+HHmm

        int t;
        int i = 0;

        t = calendar.get(Calendar.YEAR);
        append4(chars, t, i);
        i += 4;
        chars[i++] = '-';

        t = calendar.get(Calendar.MONTH) + 1;
        append2(chars, t, i);
        i += 2;
        chars[i++] = '-';

        t = calendar.get(Calendar.DATE);
        append2(chars, t, i);
        i += 2;
        chars[i++] = 'T';

        t = calendar.get(Calendar.HOUR_OF_DAY);
        append2(chars, t, i);
        i += 2;
        chars[i++] = ':';
        t = calendar.get(Calendar.MINUTE);
        append2(chars, t, i);
        i += 2;
        chars[i++] = ':';
        t = calendar.get(Calendar.SECOND);
        append2(chars, t, i);
        i += 2;
        chars[i++] = '.';
        t = calendar.get(Calendar.MILLISECOND);
        append3(chars, t, i);
        i += 3;
        if (offset == 0)
            chars[i] = 'Z';
        else {
            chars[i] = offset < 0 ? '-' : '+';
            int h = offset / HOUR;
            int m = (offset - h * HOUR) / MINUTE;
            append2(chars, h, i + 1);
            append2(chars, m, i + 3);
        }

        return chars;
    }


    private static void append4(char[] chars, int value, int offset) {
        chars[offset + 3] = (char) ('0' + (value % 10));
        value /= 10;
        if (value > 0)
            chars[offset + 2] = (char) ('0' + (value % 10));
        else {
            chars[offset + 2] = '0';
            chars[offset + 1] = '0';
            chars[offset] = '0';
        }

        value /= 10;
        if (value > 0)
            chars[offset + 1] = (char) ('0' + (value % 10));
        else {
            chars[offset + 1] = '0';
            chars[offset] = '0';
        }

        value /= 10;
        if (value > 0)
            chars[offset] = (char) ('0' + (value % 10));
        else
            chars[offset] = '0';
    }

    private static void append3(char[] chars, int value, int offset) {
        chars[offset + 2] = (char) ('0' + (value % 10));

        value /= 10;
        if (value > 0)
            chars[offset + 1] = (char) ('0' + (value % 10));
        else {
            chars[offset + 1] = '0';
            chars[offset] = '0';
        }

        value /= 10;
        if (value > 0)
            chars[offset] = (char) ('0' + (value % 10));
        else
            chars[offset] = '0';
    }

    private static void append2(char[] chars, int value, int offset) {
        chars[offset + 1] = (char) ('0' + (value % 10));

        value /= 10;
        if (value > 0)
            chars[offset] = (char) ('0' + (value % 10));
        else
            chars[offset] = '0';
    }

    private static class SimpleTimeZone extends TimeZone {
        private int rawOffset;

        private SimpleTimeZone(int rawOffset) {
            this.rawOffset = rawOffset;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        @Override
        public void setRawOffset(int offsetMillis) {
            rawOffset = offsetMillis;
        }

        @Override
        public int getRawOffset() {
            return rawOffset;
        }

        @Override
        public boolean useDaylightTime() {
            return false;
        }

        @Override
        public boolean inDaylightTime(Date date) {
            return false;
        }
    }
}
