package com.wizzardo.tools.misc;

import com.wizzardo.tools.reflection.StringReflection;

import java.util.*;

/**
 * @author: wizzardo
 * Date: 6/5/14
 */
public class DateIso8601 {

    private static int[] daysToMonth365 = new int[]{0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};
    private static int[] daysToMonth366 = new int[]{0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366};
    private static final int LEAP_YEARS_BEFORE_1970 = 1970 / 4 - 1970 / 100 + 1970 / 400;
    private static final TimeZone Z = TimeZone.getTimeZone("GMT");
    private static volatile TimeZone defaultTimezone = TimeZone.getDefault();
    private static final int HOUR = 1000 * 60 * 60;
    private static final int MINUTE = 1000 * 60;

    protected TimeZone tz = defaultTimezone;

    public DateIso8601() {
    }

    public DateIso8601(TimeZone tz) {
        this.tz = tz;
    }

    public static void setDefaultTimezone(TimeZone tz) {
        defaultTimezone = tz;
    }

    public static TimeZone getDefaultTimezone() {
        return defaultTimezone;
    }

    /**
     * @param s should be in format YYYY-MM-DDTHH:mm:ss.sssZ
     */
    public Date parse(String s) {
        int length = s.length();
        char[] chars = StringReflection.chars(s);
        int i = length == chars.length ? 0 : StringReflection.offset(s);
        int year, month, day;
        year = getInt4(chars, i);
        i = 4;
        char c = chars[i];
        if (c == '-') {
            i++;
            c = chars[i];
        }

        month = getInt2(chars, i, c);
        i += 2;

        c = chars[i];
        if (c == '-') {
            i++;
            c = chars[i];
        }

        day = getInt2(chars, i, c);
        i += 2;

        if (i >= length) {
            return new Date(dateToMillis(year, month, day));
        }

        int hour, minute = 0, second = 0;
        int millisecond = 0;

        checkOr(chars[i], 'T', ' ');
        hour = getInt2(chars, i + 1);

        i += 3;

        if (i >= length) {
            long timeStamp = getTimeStamp(year, month, day, hour, 0, 0, 0);
            return new Date(timeStamp - tz.getOffset(timeStamp));
        }

        c = chars[i];
        if (c == ':') {
            i++;
            c = chars[i];
        }
        if (isInt(c)) {
            minute = getInt2(chars, i, c);
            i += 2;

            if (i >= length) {
                long timeStamp = getTimeStamp(year, month, day, hour, minute, 0, 0);
                return new Date(timeStamp - tz.getOffset(timeStamp));
            }

            c = chars[i];
            if (c == ':') {
                i++;
                c = chars[i];
            }

            if (isInt(c)) {
                second = getInt2(chars, i, c);
                i += 2;

                if (i >= length) {
                    long timeStamp = getTimeStamp(year, month, day, hour, minute, second, 0);
                    return new Date(timeStamp - tz.getOffset(timeStamp));
                }

                c = chars[i];
                if (c == '.') {
                    i++;
                    c = chars[i];
                }
                if (isInt(c)) {
                    millisecond = getInt3(chars, i, c);
                    i += 3;
                }
            }
        }


        if (i == length) {
            long timeStamp = getTimeStamp(year, month, day, hour, minute, second, millisecond);
            return new Date(timeStamp - tz.getOffset(timeStamp));
        }
        if (chars[i] == 'Z') {
            return new Date(getTimeStamp(year, month, day, hour, minute, second, millisecond));
        }

        c = chars[i];
        boolean plus = c == '+';
        if (!plus)
            check(c, '-');

        int hours = getInt2(chars, i + 1);
        i += 3;
        if (i >= length) {
            return new Date(getTimeStamp(year, month, day, hour - hours * (plus ? 1 : -1), minute, second, millisecond));
        }
        c = chars[i];
        if (c == ':') {
            i++;
            c = chars[i];
        }

        int minutes = getInt2(chars, i, c);
        return new Date(getTimeStamp(year, month, day, hour - hours * (plus ? 1 : -1), minute - minutes * (plus ? 1 : -1), second, millisecond));
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
        long timestamp = date.getTime();
        int offset = timeZone.getOffset(timestamp);
        timestamp += offset;
        int days = (int) (timestamp / 86400000L);

        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        int milliseconds;

        if (timestamp >= 0) {
            year = days / 365 + 1970;
            if (days < daysToYear(year))
                year--;

            days -= daysToYear(year);

            month = days / 31;
            day = days - daysToMonth(year, month) + 1;
            if (day <= 0)
                day = days - daysToMonth(year, month - 1) + 1;
            else
                month++;

            milliseconds = (int) (timestamp - (timestamp / 86400000L) * 86400000L);
            hour = milliseconds / 3600000;
            milliseconds -= hour * 3600000;
            minute = milliseconds / 60000;
            milliseconds -= minute * 60000;
            second = milliseconds / 1000;
            milliseconds -= second * 1000;
        } else {
            year = days / 365 + 1970;
            timestamp = -timestamp;
            int dd = days - daysToYear(year);
            if (dd <= 0 || dd >= (isLeapYear(year) ? 366 : 365))
                dd = days - daysToYear(--year);

            days = dd;

            month = days / 31;
            day = days - daysToMonth(year, month);
            if (day <= 0)
                day = days - daysToMonth(year, month - 1);
            else
                month++;

            milliseconds = 24 * 3600000 - (int) (timestamp - (timestamp / 86400000L) * 86400000L);
            hour = milliseconds / 3600000;
            milliseconds -= hour * 3600000;
            minute = milliseconds / 60000;
            milliseconds -= minute * 60000;
            second = milliseconds / 1000;
            milliseconds -= second * 1000;
        }


        char[] chars;
        if (offset == 0)
            chars = new char[24]; // YYYY-MM-DDTHH:mm:ss.SSSZ
        else
            chars = new char[28]; // YYYY-MM-DDTHH:mm:ss.SSS+HHmm

        append4(chars, year, 0);
        chars[4] = '-';
        append2(chars, month, 5);
        chars[7] = '-';
        append2(chars, day, 8);
        chars[10] = 'T';

        append2(chars, hour, 11);
        chars[13] = ':';
        append2(chars, minute, 14);
        chars[16] = ':';
        append2(chars, second, 17);
        chars[19] = '.';
        append3(chars, milliseconds, 20);
        if (offset == 0)
            chars[23] = 'Z';
        else {
            chars[23] = offset < 0 ? '-' : '+';
            int h = offset / HOUR;
            int m = (offset - h * HOUR) / MINUTE;
            append2(chars, h, 24);
            append2(chars, m, 26);
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

    static int daysToYear(int year) {
        return (year - 1970) * 365 + year / 4 - year / 100 + year / 400 - LEAP_YEARS_BEFORE_1970 - (year % 4 == 0 ? 1 : 0) + (year % 100 == 0 && year % 400 != 0 ? 1 : 0);
    }

    public static long getTimeStamp(int year, int month, int day, int hour, int minute, int second, int milliseconds) {
        long timestamp = dateToMillis(year, month, day) + timeToMillis(hour, minute, second);
        return timestamp + milliseconds;
    }

    public static boolean isLeapYear(int year) {
        if (year < 1 || year > 9999)
            throw new IllegalArgumentException("Bad year: " + year);

        if (year % 4 != 0)
            return false;

        if (year % 100 == 0)
            return year % 400 == 0;

        return true;
    }

    public static long dateToMillis(int year, int month, int day) {
        if (year >= 1 && year <= 9999 && month >= 1 && month <= 12) {
            int[] daysToMonth = daysToMonth(year);
            if (day >= 1 && day <= daysToMonth[month] - daysToMonth[month - 1]) {
                return (daysToYear(year) + daysToMonth[month - 1] + day - 1) * 86400000L;
            }
        }
        throw new IllegalArgumentException();
    }

    private static int[] daysToMonth(int year) {
        return isLeapYear(year) ? daysToMonth366 : daysToMonth365;
    }

    private static int daysToMonth(int year, int month) {
        return daysToMonth(year)[month];
    }

    public static long timeToMillis(int hour, int minute, int second) {
        return (hour * 3600L + minute * 60L + second) * 1000;
    }
}
