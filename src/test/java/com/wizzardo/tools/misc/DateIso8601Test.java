package com.wizzardo.tools.misc;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static com.wizzardo.tools.misc.DateIso8601.*;
import static org.junit.Assert.assertEquals;

/**
 * @author: wizzardo
 * Date: 6/5/14
 */
public class DateIso8601Test {

    @Test
    public void test() {
        TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals(create(2007, 4, 5, 12, 0, 10, 123), new DateIso8601(tz).parse("2007-04-05T14:30:10.123+02:30"));
        assertEquals(create(2007, 4, 5, 17, 0, 10, 123), new DateIso8601(tz).parse("2007-04-05T14:30:10.123-0230"));
        assertEquals(create(2007, 4, 5, 14, 30, 10, 123), new DateIso8601(tz).parse("2007-04-05T14:30:10.123Z"));
        assertEquals(create(2007, 4, 5, 12, 30, 10, 123), new DateIso8601(tz).parse("2007-04-05T14:30:10.123"));
        assertEquals(create(2007, 4, 5, 14, 30, 10, 0), new DateIso8601(tz).parse("2007-04-05T14:30:10Z"));
        assertEquals(create(2007, 4, 5, 12, 30, 10, 0), new DateIso8601(tz).parse("2007-04-05T14:30:10"));
        assertEquals(create(2007, 4, 5, 14, 30, 10, 0), new DateIso8601(tz).parse("20070405T143010Z"));
        assertEquals(create(2007, 4, 5, 12, 30, 10, 0), new DateIso8601(tz).parse("20070405T143010"));
        assertEquals(create(2007, 4, 5, 14, 30, 0, 0), new DateIso8601(tz).parse("2007-04-05T14:30Z"));
        assertEquals(create(2007, 4, 5, 12, 30, 0, 0), new DateIso8601(tz).parse("2007-04-05T14:30"));
        assertEquals(create(2007, 4, 5, 14, 30, 0, 0), new DateIso8601(tz).parse("20070405T1430Z"));
        assertEquals(create(2007, 4, 5, 12, 30, 0, 0), new DateIso8601(tz).parse("20070405T1430"));
        assertEquals(create(2007, 4, 5, 14, 0, 0, 0), new DateIso8601(tz).parse("2007-04-05T14Z"));
        assertEquals(create(2007, 4, 5, 12, 0, 0, 0), new DateIso8601(tz).parse("2007-04-05T14"));
        assertEquals(create(2007, 4, 5, 14, 0, 0, 0), new DateIso8601(tz).parse("20070405T14Z"));
        assertEquals(create(2007, 4, 5, 12, 0, 0, 0), new DateIso8601(tz).parse("20070405T14"));
        assertEquals(create(2007, 4, 5, 12, 0, 0, 0), new DateIso8601(tz).parse("20070405T14+02"));
        assertEquals(create(2007, 4, 5, 0, 0, 0, 0), new DateIso8601(tz).parse("2007-04-05"));
        assertEquals(create(2007, 4, 5, 0, 0, 0, 0), new DateIso8601(tz).parse("20070405"));
    }

    @Test
    public void test_2() {
        TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        for (int i = 1900; i <= 2100; i++) {
            assertEquals(create(i, 1, 5, 14, 30, 10, 123, tz), new DateIso8601(tz).parse(i + "-01-05T14:30:10.123"));
            assertEquals(create(i, 7, 5, 14, 30, 10, 123, tz), new DateIso8601(tz).parse(i + "-07-05T14:30:10.123"));
            assertEquals(create(i, 1, 5, 14, 30, 10, 123), new DateIso8601(gmt).parse(i + "-01-05T14:30:10.123"));
            assertEquals(create(i, 7, 5, 14, 30, 10, 123), new DateIso8601(gmt).parse(i + "-07-05T14:30:10.123"));
            assertEquals(create(i, 1, 5, 14, 30, 10, 123), new DateIso8601(tz).parse(i + "-01-05T14:30:10.123Z"));
            assertEquals(create(i, 7, 5, 14, 30, 10, 123), new DateIso8601(tz).parse(i + "-07-05T14:30:10.123Z"));
        }
    }

    private Date create(int year, int month, int day, int hours, int minutes, int seconds, int milliseconds) {
        return create(year, month, day, hours, minutes, seconds, milliseconds, TimeZone.getTimeZone("GMT"));
    }

    private Date create(int year, int month, int day, int hours, int minutes, int seconds, int milliseconds, TimeZone tz) {
        Calendar c = GregorianCalendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, seconds);
        c.set(Calendar.MILLISECOND, milliseconds);
        c.setTimeZone(tz);
        return c.getTime();
    }

    @Test
    public void testFormat() {
        String s = "2007-04-05T14:30:10.123Z";

        assertEquals(s, format(new DateIso8601().parse(s)));

        s = "2007-04-05T14:30:10.123-0230";
        assertEquals("2007-04-05T17:00:10.123Z", format(new DateIso8601().parse(s)));
        assertEquals("2007-04-05T19:00:10.123+0200", format(new DateIso8601().parse(s), TimeZone.getTimeZone("Europe/Berlin")));
    }
}
