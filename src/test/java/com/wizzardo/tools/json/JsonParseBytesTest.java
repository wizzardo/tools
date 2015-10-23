package com.wizzardo.tools.json;

import org.junit.Assert;
import org.junit.Test;

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
}
