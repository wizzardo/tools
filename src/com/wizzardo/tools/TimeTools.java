package com.wizzardo.tools;

import java.util.Date;

/**
 * @author: moxa
 * Date: 10/22/13
 */
public class TimeTools {

    public static enum Unit {

        MILLISECOND(1),
        SECOND(1000),
        MINUTE(1000 * 60),
        HOUR(1000l * 60 * 60),
        DAY(1000l * 60 * 60 * 24),
        WEEK(1000l * 60 * 60 * 24 * 7);

        private final long milliseconds;

        private Unit(long milliseconds) {
            this.milliseconds = milliseconds;
        }

        public long toMilliseconds(long time) {
            return time * milliseconds;
        }

        public long fromMilliseconds(long time) {
            return time / milliseconds;
        }

        public long to(long time) {
            return toMilliseconds(time);
        }

        public long from(long time) {
            return fromMilliseconds(time);
        }

        public long mod(long time) {
            return time % milliseconds;
        }
    }

    public static long add(long time, long add, Unit units) {
        return time + units.to(add);
    }

    public static Date add(Date time, long add, Unit units) {
        return new Date(add(time.getTime(), add, units));
    }

}
