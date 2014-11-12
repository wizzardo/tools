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
    }
}
