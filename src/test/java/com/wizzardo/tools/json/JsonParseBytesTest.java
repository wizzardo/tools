package com.wizzardo.tools.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * Created by wizzardo on 14.09.15.
 */
public class JsonParseBytesTest {
    static class TestBinder implements JsonBinder {
        Object value;

        @Override
        public void add(Object value) {
            this.value = value;
        }

        @Override
        public void add(JsonItem value) {
            this.value = value;
        }

        @Override
        public Object getObject() {
            return null;
        }

        @Override
        public JsonBinder getObjectBinder() {
            return null;
        }

        @Override
        public JsonBinder getArrayBinder() {
            return null;
        }

        @Override
        public void setTemporaryKey(String key) {

        }

        @Override
        public JsonFieldSetter getFieldSetter() {
            return null;
        }
    }

    @Test
    public void parse_number_1() {
        byte[] data = "123,".getBytes();

        TestBinder binder = new TestBinder();
        JsonUtils.parseNumber(binder, data, 0, data.length, new NumberParsingContext());

        Assert.assertEquals(123l, binder.value);
    }

    @Test
    public void parse_number_2() {
        byte[] data = "123.1,".getBytes();

        TestBinder binder = new TestBinder();
        JsonUtils.parseNumber(binder, data, 0, data.length, new NumberParsingContext());

        Assert.assertEquals(123.1, binder.value);
    }

    @Test
    public void parse_number_3() {
        byte[] data = "123,".getBytes();

        TestBinder binder = new TestBinder();
        JsonUtils.parseNumber(binder, data, 0, data.length, new NumberParsingContext());
        Assert.assertEquals(123l, binder.value);

        data = "1234}".getBytes();
        JsonUtils.parseNumber(binder, data, 0, data.length, new NumberParsingContext());
        Assert.assertEquals(1234l, binder.value);

        data = "12345]".getBytes();
        JsonUtils.parseNumber(binder, data, 0, data.length, new NumberParsingContext());
        Assert.assertEquals(12345l, binder.value);

        data = "123456\n".getBytes();
        JsonUtils.parseNumber(binder, data, 0, data.length, new NumberParsingContext());
        Assert.assertEquals(123456l, binder.value);

        data = "1234567\r".getBytes();
        JsonUtils.parseNumber(binder, data, 0, data.length, new NumberParsingContext());
        Assert.assertEquals(1234567l, binder.value);

        data = "12345678 ".getBytes();
        JsonUtils.parseNumber(binder, data, 0, data.length, new NumberParsingContext());
        Assert.assertEquals(12345678l, binder.value);
    }

    @Test
    public void reset_number_context() {
        NumberParsingContext context = new NumberParsingContext();

        Assert.assertEquals(false, context.done);
        Assert.assertEquals(false, context.started);
        Assert.assertEquals(false, context.negative);
        Assert.assertEquals(false, context.floatValue);
        Assert.assertEquals(0, context.big);
        Assert.assertEquals(0, context.l);
        Assert.assertEquals(0, context.fractional);

        context.negative = true;
        context.started = true;
        context.done = true;
        context.floatValue = true;
        context.big = 1;
        context.l = 2;
        context.fractional = 3;

        Assert.assertEquals(true, context.done);
        Assert.assertEquals(true, context.started);
        Assert.assertEquals(true, context.negative);
        Assert.assertEquals(true, context.floatValue);
        Assert.assertEquals(1, context.big);
        Assert.assertEquals(2, context.l);
        Assert.assertEquals(3, context.fractional);

        context.reset();

        Assert.assertEquals(false, context.done);
        Assert.assertEquals(false, context.started);
        Assert.assertEquals(false, context.negative);
        Assert.assertEquals(false, context.floatValue);
        Assert.assertEquals(0, context.big);
        Assert.assertEquals(0, context.l);
        Assert.assertEquals(0, context.fractional);
    }

    @Test
    public void reset_string_context() {
        StringParsingContext context = new StringParsingContext();

        Assert.assertEquals(false, context.done);
        Assert.assertEquals(false, context.started);
        Assert.assertEquals(false, context.escape);
        Assert.assertEquals(false, context.needDecoding);
        Assert.assertEquals(0, context.quote);
        Assert.assertEquals(0, context.length);
        Assert.assertEquals(16, context.buffer.length);

        context.started = true;
        context.done = true;
        context.escape = true;
        context.needDecoding = true;
        context.quote = 1;
        context.length = 2;

        Assert.assertEquals(true, context.done);
        Assert.assertEquals(true, context.started);
        Assert.assertEquals(true, context.escape);
        Assert.assertEquals(true, context.needDecoding);
        Assert.assertEquals(1, context.quote);
        Assert.assertEquals(2, context.length);

        context.reset();

        Assert.assertEquals(false, context.done);
        Assert.assertEquals(false, context.started);
        Assert.assertEquals(false, context.escape);
        Assert.assertEquals(false, context.needDecoding);
        Assert.assertEquals(0, context.quote);
        Assert.assertEquals(0, context.length);
        Assert.assertEquals(16, context.buffer.length);
    }

    @Test
    public void populate_string_context_buffer() {
        StringParsingContext context = new StringParsingContext();

        Assert.assertEquals(0, context.length);
        Assert.assertEquals(16, context.buffer.length);

        for (int i = 0; i < 32; i++) {
            context.put(new byte[]{(byte) i}, 0, 1);
        }

        Assert.assertEquals(32, context.length);
        Assert.assertTrue(context.buffer.length >= context.length);
    }

    @Test
    public void test_isTrue() {
        StringParsingContext context = new StringParsingContext();
        byte[] bytes = new byte[]{'t', 'r', 'u', 'e'};

        Assert.assertTrue(JsonUtils.isTrue(bytes, 0, 4, context));
        Assert.assertFalse(JsonUtils.isTrue(bytes, 0, 3, context));
        Assert.assertFalse(JsonUtils.isTrue(bytes, 1, 3, context));

        context.put(bytes, 0, 1);
        Assert.assertTrue(JsonUtils.isTrue(bytes, 1, 3, context));

        context.put(bytes, 1, 2);
        Assert.assertTrue(JsonUtils.isTrue(bytes, 2, 2, context));

        context.put(bytes, 2, 3);
        Assert.assertTrue(JsonUtils.isTrue(bytes, 3, 1, context));

        context.put(bytes, 3, 4);
        Assert.assertTrue(JsonUtils.isTrue(bytes, 4, 0, context));
    }

    @Test
    public void test_isNull() {
        StringParsingContext context = new StringParsingContext();
        byte[] bytes = new byte[]{'n', 'u', 'l', 'l'};

        Assert.assertTrue(JsonUtils.isNull(bytes, 0, 4, context));
        Assert.assertFalse(JsonUtils.isNull(bytes, 0, 3, context));
        Assert.assertFalse(JsonUtils.isNull(bytes, 1, 3, context));

        context.put(bytes, 0, 1);
        Assert.assertTrue(JsonUtils.isNull(bytes, 1, 3, context));

        context.put(bytes, 1, 2);
        Assert.assertTrue(JsonUtils.isNull(bytes, 2, 2, context));

        context.put(bytes, 2, 3);
        Assert.assertTrue(JsonUtils.isNull(bytes, 3, 1, context));

        context.put(bytes, 3, 4);
        Assert.assertTrue(JsonUtils.isNull(bytes, 4, 0, context));
    }

    @Test
    public void test_isFalse() {
        StringParsingContext context = new StringParsingContext();
        byte[] bytes = new byte[]{'f', 'a', 'l', 's', 'e'};

        Assert.assertTrue(JsonUtils.isFalse(bytes, 0, 5, context));
        Assert.assertFalse(JsonUtils.isFalse(bytes, 0, 4, context));
        Assert.assertFalse(JsonUtils.isFalse(bytes, 1, 4, context));

        context.put(bytes, 0, 1);
        Assert.assertTrue(JsonUtils.isFalse(bytes, 1, 4, context));

        context.put(bytes, 1, 2);
        Assert.assertTrue(JsonUtils.isFalse(bytes, 2, 3, context));

        context.put(bytes, 2, 3);
        Assert.assertTrue(JsonUtils.isFalse(bytes, 3, 2, context));

        context.put(bytes, 3, 4);
        Assert.assertTrue(JsonUtils.isFalse(bytes, 4, 1, context));

        context.put(bytes, 4, 5);
        Assert.assertTrue(JsonUtils.isFalse(bytes, 5, 0, context));
    }

    @Test
    public void test_unescape_1() throws UnsupportedEncodingException {
        byte[] data = "foo".getBytes("utf-8");

        Assert.assertEquals("foo", JsonTools.unescape(data, 0, data.length, new StringParsingContext()));
    }

    @Test
    public void test_unescape_2() throws UnsupportedEncodingException {
        byte[] data = "foo\\nbar".getBytes("utf-8");

        Assert.assertEquals("foo\nbar", JsonTools.unescape(data, 0, data.length, new StringParsingContext()));
    }

    @Test
    public void test_unescape_3() throws UnsupportedEncodingException {
        byte[] data = "foo\\nbar".getBytes("utf-8");

        StringParsingContext context = new StringParsingContext();
        context.put(data, 0, 3);
        Assert.assertEquals("foo\nbar", JsonTools.unescape(data, 3, data.length, context));
    }

    @Test
    public void test_unescape_4() throws UnsupportedEncodingException {
        byte[] data = "foo\\nbar".getBytes("utf-8");

        StringParsingContext context = new StringParsingContext();
        context.put(data, 0, 5);
        Assert.assertEquals("foo\nbar", JsonTools.unescape(data, 5, data.length, context));
    }

    @Test
    public void test_unescape_5() throws UnsupportedEncodingException {
        byte[] data = "foo\\nbar".getBytes("utf-8");

        StringParsingContext context = new StringParsingContext();
        context.put(data, 0, 4);
        Assert.assertEquals("foo\nbar", JsonTools.unescape(data, 4, data.length, context));
    }
}
