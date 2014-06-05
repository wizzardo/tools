package com.wizzardo.tools.misc;

import java.util.*;

/**
 * @author: wizzardo
 * Date: 6/5/14
 */
public class DateIso8601 {

    private static final TimeZone Z = TimeZone.getTimeZone("GMT");

    /**
     * @param s should be in format YYYY-MM-DDTHH:mm:ss.sssZ
     */
    public static Date parse(String s) {
        //YYYY-MM-DDTHH:mm:ss.sssZ
        Calendar calendar = GregorianCalendar.getInstance();
        int length = s.length();
        int i;


        calendar.set(Calendar.YEAR, getInt4(s, 0));
        i = 4;
        char c = s.charAt(i);
        if (c == '-') {
            i++;
            c = s.charAt(i);
        }
        calendar.set(Calendar.MONTH, getInt2(s, i, c) - 1);
        i += 2;

        c = s.charAt(i);
        if (c == '-') {
            i++;
            c = s.charAt(i);
        }

        calendar.set(Calendar.DAY_OF_MONTH, getInt2(s, i, c));
        i += 2;

        if (i >= length) {
            clearTime(calendar);
            return calendar.getTime();
        }

        check(s.charAt(i), 'T');
        calendar.set(Calendar.HOUR_OF_DAY, getInt2(s, i + 1));

        i += 3;

        c = s.charAt(i);
        if (c == ':') {
            i++;
            c = s.charAt(i);
        }
        if (isInt(c)) {
            calendar.set(Calendar.MINUTE, getInt2(s, i, c));
            i += 2;

            c = s.charAt(i);
            if (c == ':') {
                i++;
                c = s.charAt(i);
            }

            if (isInt(c)) {
                calendar.set(Calendar.SECOND, getInt2(s, i, c));
                i += 2;


                c = s.charAt(i);
                if (c == '.') {
                    i++;
                    c = s.charAt(i);
                }
                if (isInt(c)) {
                    calendar.set(Calendar.MILLISECOND, getInt3(s, i, c));
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


        if (s.charAt(i) == 'Z') {
            calendar.setTimeZone(Z);
            return calendar.getTime();
        }

        c = s.charAt(i);
        boolean plus = c == '+';
        if (!plus)
            check(c, '-');

        int hours = getInt2(s, i + 1);
        i += 3;
        if (i >= length) {
            calendar.setTimeZone(new SimpleTimeZone((int) TimeTools.Unit.HOUR.to(hours) * (plus ? 1 : -1)));
            return calendar.getTime();
        }
        c = s.charAt(i);
        if (c == ':') {
            i++;
            c = s.charAt(i);
        }

        int minutes = getInt2(s, i, c);
        calendar.setTimeZone(new SimpleTimeZone((int) (TimeTools.Unit.HOUR.to(hours) + TimeTools.Unit.MINUTE.to(minutes)) * (plus ? 1 : -1)));

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

    private static int getInt2(String s, int offset) {
        int i = getInt(s.charAt(offset));
        return i * 10 + getInt(s.charAt(offset + 1));
    }

    private static int getInt2(String s, int offset, char first) {
        int i = getInt(first);
        return i * 10 + getInt(s.charAt(offset + 1));
    }

    private static int getInt3(String s, int offset, char first) {
        int i = getInt(first);
        i = i * 10 + getInt(s.charAt(offset + 1));
        return i * 10 + getInt(s.charAt(offset + 2));
    }

    private static int getInt3(String s, int offset) {
        int i = getInt(s.charAt(offset));
        i = i * 10 + getInt(s.charAt(offset + 1));
        return i * 10 + getInt(s.charAt(offset + 2));
    }

    private static int getInt4(String s, int offset) {
        int i = getInt(s.charAt(offset));
        i = i * 10 + getInt(s.charAt(offset + 1));
        i = i * 10 + getInt(s.charAt(offset + 2));
        return i * 10 + getInt(s.charAt(offset + 3));
    }

    private static void clearTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.setTimeZone(Z);
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
