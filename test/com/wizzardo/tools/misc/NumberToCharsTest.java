package com.wizzardo.tools.misc;

import org.junit.Assert;
import org.junit.Test;

import static com.wizzardo.tools.misc.NumberToChars.*;

/**
 * @author: wizzardo
 * Date: 12.11.14
 */
public class NumberToCharsTest {

    @Test
    public void stringSizeOfTest() {
        int i;
        Assert.assertEquals(String.valueOf(i = 1).length(), stringSizeOf(i));
        Assert.assertEquals(String.valueOf(i = 12).length(), stringSizeOf(i));
        Assert.assertEquals(String.valueOf(i = 123).length(), stringSizeOf(i));
        Assert.assertEquals(String.valueOf(i = 1234).length(), stringSizeOf(i));
        Assert.assertEquals(String.valueOf(i = 12345).length(), stringSizeOf(i));
        Assert.assertEquals(String.valueOf(i = 123456).length(), stringSizeOf(i));
        Assert.assertEquals(String.valueOf(i = 1234567).length(), stringSizeOf(i));
        Assert.assertEquals(String.valueOf(i = 12345678).length(), stringSizeOf(i));
        Assert.assertEquals(String.valueOf(i = 123456789).length(), stringSizeOf(i));
        Assert.assertEquals(String.valueOf(i = 1234567890).length(), stringSizeOf(i));

        Assert.assertEquals(String.valueOf(i = -1).length(), stringSizeOfWithMinus(i));
        Assert.assertEquals(String.valueOf(i = -12).length(), stringSizeOfWithMinus(i));
        Assert.assertEquals(String.valueOf(i = -123).length(), stringSizeOfWithMinus(i));
        Assert.assertEquals(String.valueOf(i = -1234).length(), stringSizeOfWithMinus(i));
        Assert.assertEquals(String.valueOf(i = -12345).length(), stringSizeOfWithMinus(i));
        Assert.assertEquals(String.valueOf(i = -123456).length(), stringSizeOfWithMinus(i));
        Assert.assertEquals(String.valueOf(i = -1234567).length(), stringSizeOfWithMinus(i));
        Assert.assertEquals(String.valueOf(i = -12345678).length(), stringSizeOfWithMinus(i));
        Assert.assertEquals(String.valueOf(i = -123456789).length(), stringSizeOfWithMinus(i));
        Assert.assertEquals(String.valueOf(i = -1234567890).length(), stringSizeOfWithMinus(i));

        long l;
        Assert.assertEquals(String.valueOf(l = 1).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 12).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 123).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 1234).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 12345).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 123456).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 1234567).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 12345678).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 123456789).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 1234567890).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 12345678901l).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 123456789012l).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 1234567890123l).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 12345678901234l).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 123456789012345l).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 1234567890123456l).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 12345678901234567l).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 123456789012345678l).length(), stringSizeOf(l));
        Assert.assertEquals(String.valueOf(l = 1234567890123456789l).length(), stringSizeOf(l));

        Assert.assertEquals(String.valueOf(l = -1).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -12).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -123).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -1234).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -12345).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -123456).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -1234567).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -12345678).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -123456789).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -1234567890).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -12345678901l).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -123456789012l).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -1234567890123l).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -12345678901234l).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -123456789012345l).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -1234567890123456l).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -12345678901234567l).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -123456789012345678l).length(), stringSizeOfWithMinus(l));
        Assert.assertEquals(String.valueOf(l = -1234567890123456789l).length(), stringSizeOfWithMinus(l));

        for (int j = 1; j > 0; j *= 10) {
            Assert.assertEquals(String.valueOf(j).length(), stringSizeOf(j));
        }
        for (int j = -1; j < 0; j *= 10) {
            Assert.assertEquals(String.valueOf(j).length(), stringSizeOfWithMinus(j));
        }
        for (long j = 1; j > 0; j *= 10) {
            Assert.assertEquals(String.valueOf(j).length(), stringSizeOf(j));
        }
        for (long j = -1; j < 0; j *= 10) {
            Assert.assertEquals(String.valueOf(j).length(), stringSizeOfWithMinus(j));
        }
    }

    @Test
    public void toCharsTestInteger() {
        char[] chars = new char[11];
        int i;
        Assert.assertEquals(String.valueOf(i = 1), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = 12), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = 123), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = 1234), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = 12345), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = 123456), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = 1234567), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = 12345678), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = 123456789), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = 1234567890), new String(chars, 0, toChars(i, chars, 0)));

        Assert.assertEquals(String.valueOf(i = -1), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = -12), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = -123), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = -1234), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = -12345), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = -123456), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = -1234567), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = -12345678), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = -123456789), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = -1234567890), new String(chars, 0, toChars(i, chars, 0)));

        Assert.assertEquals(String.valueOf(i = Integer.MIN_VALUE), new String(chars, 0, toChars(i, chars, 0)));
        Assert.assertEquals(String.valueOf(i = Integer.MAX_VALUE), new String(chars, 0, toChars(i, chars, 0)));

        for (int j = 1; j > 0; j *= 10) {
            Assert.assertEquals(String.valueOf(j), new String(chars, 0, toChars(j, chars, 0)));
        }
        for (int j = -1; j < 0; j *= 10) {
            Assert.assertEquals(String.valueOf(j), new String(chars, 0, toChars(j, chars, 0)));
        }
    }

    @Test
    public void toCharsTestLong() {
        char[] chars = new char[20];
        long l;
        Assert.assertEquals(String.valueOf(l = 1), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 12), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 123), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 1234), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 12345), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 123456), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 1234567), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 12345678), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 123456789), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 1234567890), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 12345678901l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 123456789012l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 1234567890123l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 12345678901234l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 123456789012345l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 1234567890123456l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 12345678901234567l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 123456789012345678l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = 1234567890123456789l), new String(chars, 0, toChars(l, chars, 0)));

        Assert.assertEquals(String.valueOf(l = -1), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -12), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -123), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -1234), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -12345), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -123456), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -1234567), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -12345678), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -123456789), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -1234567890), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -12345678901l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -123456789012l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -1234567890123l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -12345678901234l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -123456789012345l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -1234567890123456l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -12345678901234567l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -123456789012345678l), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = -1234567890123456789l), new String(chars, 0, toChars(l, chars, 0)));

        Assert.assertEquals(String.valueOf(l = Long.MAX_VALUE), new String(chars, 0, toChars(l, chars, 0)));
        Assert.assertEquals(String.valueOf(l = Long.MIN_VALUE), new String(chars, 0, toChars(l, chars, 0)));

        for (long j = 1; j > 0; j *= 10) {
            Assert.assertEquals(String.valueOf(j), new String(chars, 0, toChars(j, chars, 0)));
        }
        for (long j = -1; j < 0; j *= 10) {
            Assert.assertEquals(String.valueOf(j), new String(chars, 0, toChars(j, chars, 0)));
        }
    }
}
