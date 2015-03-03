package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldReflection;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
abstract class JsonFieldSetter extends FieldReflection {

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
            return new BooleanBoxedSetter(f);

        if (cl == Integer.class)
            return new IntBoxedSetter(f);

        if (cl == Long.class)
            return new LongBoxedSetter(f);

        if (cl == Byte.class)
            return new ByteBoxedSetter(f);

        if (cl == Short.class)
            return new ShortBoxedSetter(f);

        if (cl == Character.class)
            return new CharBoxedSetter(f);

        if (cl == Float.class)
            return new FloatBoxedSetter(f);

        if (cl == Double.class)
            return new DoubleBoxedSetter(f);

        return new ObjectSetter(f);
    }

    public void setString(Object object, String value) {
        setObject(object, value);
    }

    abstract void set(Object object, JsonItem value);

    public static class ByteSetter extends JsonFieldSetter {
        protected ByteSetter() {
        }


        ByteSetter(Field f) {
            super(f);
        }

        public void set(Object object, JsonItem value) {
            setByte(object, value.asByte());
        }

        @Override
        public void setString(Object object, String value) {
            setByte(object, Byte.parseByte(value));
        }
    }

    public static class ShortSetter extends JsonFieldSetter {
        protected ShortSetter() {
        }

        ShortSetter(Field f) {
            super(f);
        }

        public void set(Object object, JsonItem value) {
            setShort(object, value.asShort());
        }

        @Override
        public void setString(Object object, String value) {
            setShort(object, Short.parseShort(value));
        }
    }

    public static class IntSetter extends JsonFieldSetter {
        protected IntSetter() {
        }

        IntSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setInteger(object, value.asInteger());
        }

        @Override
        public void setString(Object object, String value) {
            setInteger(object, Integer.parseInt(value));
        }
    }

    public static class LongSetter extends JsonFieldSetter {
        protected LongSetter() {
        }

        LongSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setLong(object, value.asLong());
        }

        @Override
        public void setString(Object object, String value) {
            setLong(object, Long.parseLong(value));
        }
    }

    public static class FloatSetter extends JsonFieldSetter {
        protected FloatSetter() {
        }

        FloatSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setFloat(object, value.asFloat());
        }

        @Override
        public void setString(Object object, String value) {
            setFloat(object, Float.parseFloat(value));
        }
    }

    public static class DoubleSetter extends JsonFieldSetter {
        protected DoubleSetter() {
        }

        DoubleSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setDouble(object, value.asDouble());
        }

        @Override
        public void setString(Object object, String value) {
            setDouble(object, Double.parseDouble(value));
        }
    }

    public static class CharSetter extends JsonFieldSetter {
        protected CharSetter() {
        }

        CharSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setChar(object, value.asChar());
        }

        @Override
        public void setString(Object object, String value) {
            if (value.length() > 1) {
                setChar(object, value.charAt(Integer.parseInt(value)));
            } else
                setChar(object, value.charAt(0));
        }
    }

    public static class BooleanSetter extends JsonFieldSetter {
        protected BooleanSetter() {
        }

        BooleanSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setBoolean(object, value.asBoolean());
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
        public void set(Object object, JsonItem value) {
            Object ob = value == null ? null : value.getAs(field.getType());
            setObject(object, ob);
        }
    }

    public static class EnumSetter extends JsonFieldSetter {
        protected EnumSetter() {
        }

        EnumSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            Class cl = field.getType();
            Object ob = value == null ? null : value.asEnum(cl);
            setObject(object, ob);
        }

        @Override
        public void setString(Object object, String value) {
            Class cl = field.getType();
            setObject(object, JsonUtils.asEnum(cl, value));
        }
    }

    public static class BooleanBoxedSetter extends BooleanSetter {
        protected BooleanBoxedSetter() {
        }

        BooleanBoxedSetter(Field f) {
            super(f);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setObject(object, value ? Boolean.TRUE : Boolean.FALSE);
        }

        @Override
        public Type getType() {
            return Type.BOOLEAN;
        }
    }

    public static class IntBoxedSetter extends IntSetter {
        protected IntBoxedSetter() {
        }

        IntBoxedSetter(Field f) {
            super(f);
        }

        @Override
        public void setInteger(Object object, int value) {
            setObject(object, value);
        }

        @Override
        public Type getType() {
            return Type.INTEGER;
        }
    }

    public static class LongBoxedSetter extends LongSetter {
        protected LongBoxedSetter() {
        }

        LongBoxedSetter(Field f) {
            super(f);
        }

        @Override
        public void setLong(Object object, long value) {
            setObject(object, value);
        }

        @Override
        public Type getType() {
            return Type.LONG;
        }
    }

    public static class ShortBoxedSetter extends ShortSetter {
        protected ShortBoxedSetter() {
        }

        ShortBoxedSetter(Field f) {
            super(f);
        }

        @Override
        public void setShort(Object object, short value) {
            setObject(object, value);
        }

        @Override
        public Type getType() {
            return Type.SHORT;
        }
    }

    public static class ByteBoxedSetter extends ByteSetter {
        protected ByteBoxedSetter() {
        }

        ByteBoxedSetter(Field f) {
            super(f);
        }

        @Override
        public void setByte(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        public Type getType() {
            return Type.BYTE;
        }
    }

    public static class CharBoxedSetter extends CharSetter {
        protected CharBoxedSetter() {
        }

        CharBoxedSetter(Field f) {
            super(f);
        }

        @Override
        public void setChar(Object object, char value) {
            setObject(object, value);
        }

        @Override
        public Type getType() {
            return Type.CHAR;
        }
    }

    public static class FloatBoxedSetter extends FloatSetter {
        protected FloatBoxedSetter() {
        }

        FloatBoxedSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setObject(object, StringConverter.toFloat(value));
        }

        @Override
        public void setFloat(Object object, float value) {
            setObject(object, value);
        }

        @Override
        public Type getType() {
            return Type.FLOAT;
        }
    }

    public static class DoubleBoxedSetter extends DoubleSetter {
        protected DoubleBoxedSetter() {
        }

        DoubleBoxedSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setObject(object, StringConverter.toDouble(value));
        }

        @Override
        public void setDouble(Object object, double value) {
            setObject(object, value);
        }

        @Override
        public Type getType() {
            return Type.DOUBLE;
        }
    }
}
