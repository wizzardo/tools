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
}
