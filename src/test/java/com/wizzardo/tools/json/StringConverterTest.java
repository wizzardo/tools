package com.wizzardo.tools.json;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static com.wizzardo.tools.json.StringConverter.*;

/**
 * Created by wizzardo on 07.03.15.
 */
public class StringConverterTest {

    @Test
    public void test_get_converter() {
        Assert.assertEquals(TO_BOOLEAN, getConverter(Boolean.class));
        Assert.assertEquals(TO_INTEGER, getConverter(Integer.class));
        Assert.assertEquals(TO_LONG, getConverter(Long.class));
        Assert.assertEquals(TO_BYTE, getConverter(Byte.class));
        Assert.assertEquals(TO_SHORT, getConverter(Short.class));
        Assert.assertEquals(TO_FLOAT, getConverter(Float.class));
        Assert.assertEquals(TO_DOUBLE, getConverter(Double.class));
        Assert.assertEquals(TO_DATE, getConverter(Date.class));

        Assert.assertEquals(TO_STRING, getConverter(String.class));
        Assert.assertEquals(TO_STRING, getConverter(Object.class));
    }

    @Test
    public void test_convert() {
        Assert.assertEquals(Boolean.TRUE, getConverter(Boolean.class).convert("true"));
        Assert.assertEquals(Integer.valueOf(1), getConverter(Integer.class).convert("1"));
        Assert.assertEquals(Long.valueOf(1l), getConverter(Long.class).convert("1"));
        Assert.assertEquals(Byte.valueOf((byte) 1), getConverter(Byte.class).convert("1"));
        Assert.assertEquals(Short.valueOf((short) 1), getConverter(Short.class).convert("1"));
        Assert.assertEquals(Float.valueOf(1.0f), getConverter(Float.class).convert("1.0"));
        Assert.assertEquals(Double.valueOf(1.0), getConverter(Double.class).convert("1.0"));
        Assert.assertEquals(new Date(1175783410123l), getConverter(Date.class).convert("2007-04-05T14:30:10.123Z"));

        Assert.assertEquals("foobar", getConverter(String.class).convert("foobar"));
        Assert.assertEquals("foobar", getConverter(Object.class).convert("foobar"));
    }
}
