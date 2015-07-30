package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.field.AbstractGetterSetter;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
abstract class JsonFieldSetter extends AbstractGetterSetter {

    protected JsonFieldSetter() {
    }

    protected JsonFieldSetter(Field f) {
        super(f);
    }

    public static JsonFieldSetter createSetter(Field f) {
        Class cl = f.getType();
        if (cl == int.class)
            return new IntSetter(f);

        if (cl == long.class)
            return new LongSetter(f);

        if (cl == byte.class)
            return new ByteSetter(f);

        if (cl == short.class)
            return new ShortSetter(f);

        if (cl == float.class)
            return new FloatSetter(f);

        if (cl == double.class)
            return new DoubleSetter(f);

        if (cl == char.class)
            return new CharSetter(f);

        if (cl == boolean.class)
            return new BooleanSetter(f);

        if (cl.isEnum())
            return new EnumSetter(f);

        if (cl == Boolean.class)
            return new BoxedSetter(f, StringConverter.TO_BOOLEAN);

        if (cl == Integer.class)
            return new BoxedSetter(f, StringConverter.TO_INTEGER);

        if (cl == Long.class)
            return new BoxedSetter(f, StringConverter.TO_LONG);

        if (cl == Byte.class)
            return new BoxedSetter(f, StringConverter.TO_BYTE);

        if (cl == Short.class)
            return new BoxedSetter(f, StringConverter.TO_SHORT);

        if (cl == Character.class)
            return new BoxedSetter(f, StringConverter.TO_CHARACTER);

        if (cl == Float.class)
            return new BoxedSetter(f, StringConverter.TO_FLOAT);

        if (cl == Double.class)
            return new BoxedSetter(f, StringConverter.TO_DOUBLE);

        return new ObjectSetter(f);
    }

    public void setString(Object object, String value) {
        setObject(object, value);
    }

    public static class ByteSetter extends JsonFieldSetter {

        ByteSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setByte(object, Byte.parseByte(value));
        }
    }

    public static class ShortSetter extends JsonFieldSetter {

        ShortSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setShort(object, Short.parseShort(value));
        }
    }

    public static class IntSetter extends JsonFieldSetter {

        IntSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setInteger(object, Integer.parseInt(value));
        }
    }

    public static class LongSetter extends JsonFieldSetter {

        LongSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setLong(object, Long.parseLong(value));
        }
    }

    public static class FloatSetter extends JsonFieldSetter {
        FloatSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setFloat(object, Float.parseFloat(value));
        }
    }

    public static class DoubleSetter extends JsonFieldSetter {

        DoubleSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setDouble(object, Double.parseDouble(value));
        }
    }

    public static class CharSetter extends JsonFieldSetter {

        CharSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            if (value.length() > 1) {
                setChar(object, (char) Integer.parseInt(value));
            } else
                setChar(object, value.charAt(0));
        }
    }

    public static class BooleanSetter extends JsonFieldSetter {

        BooleanSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setBoolean(object, Boolean.parseBoolean(value));
        }
    }

    public static class ObjectSetter extends JsonFieldSetter {
        protected ObjectSetter() {
        }

        ObjectSetter(Field f) {
            super(f);
        }

        @Override
        public void setInteger(Object object, int value) {
            setObject(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setObject(object, value);
        }

        @Override
        public void setByte(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setObject(object, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setObject(object, value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setObject(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setObject(object, value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setObject(object, value);
        }
    }

    public static class EnumSetter extends JsonFieldSetter {

        EnumSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            Class cl = field.getType();
            setObject(object, JsonUtils.asEnum(cl, value));
        }
    }

    public static class BoxedSetter extends ObjectSetter {

        private final StringConverter converter;

        BoxedSetter(Field f, StringConverter converter) {
            super(f);
            this.converter = converter;
        }

        @Override
        public void setString(Object object, String value) {
            setObject(object, converter.convert(value));
        }

        @Override
        public Type getType() {
            return converter.type;
        }
    }
}
