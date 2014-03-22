package com.wizzardo.tools.json;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
abstract class FieldSetter {

    void set(Field f, Object object, JsonItem value) {
        try {
            doSet(f, object, value);
        } catch (IllegalAccessException ignore) {
        }
    }

    static FieldSetter getSetter(Class cl) {
        if (cl == int.class)
            return intSetter;

        if (cl == long.class)
            return longSetter;

        if (cl == byte.class)
            return byteSetter;

        if (cl == short.class)
            return shortSetter;

        if (cl == float.class)
            return floatSetter;

        if (cl == double.class)
            return doubleSetter;

        if (cl == char.class)
            return charSetter;

        if (cl == boolean.class)
            return booleanSetter;

        return objectSetter;
    }

    protected abstract void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException;


    private static FieldSetter byteSetter = new FieldSetter() {
        @Override
        public void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException {
            f.setByte(object, value.asByte((byte) 0));
        }
    };

    private static FieldSetter shortSetter = new FieldSetter() {
        @Override
        public void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException {
            f.setShort(object, value.asShort((short) 0));
        }
    };
    private static FieldSetter intSetter = new FieldSetter() {
        @Override
        public void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException {
            f.setInt(object, value.asInteger(0));
        }
    };

    private static FieldSetter longSetter = new FieldSetter() {
        @Override
        public void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException {
            f.setLong(object, value.asLong(0l));
        }
    };

    private static FieldSetter floatSetter = new FieldSetter() {
        @Override
        public void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException {
            f.setFloat(object, value.asFloat(0f));
        }
    };

    private static FieldSetter doubleSetter = new FieldSetter() {
        @Override
        public void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException {
            f.setDouble(object, value.asDouble(0d));
        }
    };

    private static FieldSetter charSetter = new FieldSetter() {
        @Override
        public void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException {
            f.setChar(object, (char) (int) value.asInteger(0));
        }
    };

    private static FieldSetter booleanSetter = new FieldSetter() {
        @Override
        public void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException {
            f.setBoolean(object, value.asBoolean(Boolean.FALSE));
        }
    };

    private static FieldSetter objectSetter = new FieldSetter() {
        @Override
        public void doSet(Field f, Object object, JsonItem value) throws IllegalAccessException {
            f.set(object, value.getAs(f.getType()));
        }
    };
}
