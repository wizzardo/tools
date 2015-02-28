package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldReflection;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
abstract class JsonFieldSetter extends FieldReflection {

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

    abstract void set(Object object, JsonItem value);

    public static class ByteSetter extends JsonFieldSetter {

        ByteSetter(Field f) {
            super(f);
        }

        public void set(Object object, JsonItem value) {
            setByte(object, value.asByte());
        }
    }

    public static class ShortSetter extends JsonFieldSetter {
        ShortSetter(Field f) {
            super(f);
        }

        public void set(Object object, JsonItem value) {
            setShort(object, value.asShort());
        }
    }

    public static class IntSetter extends JsonFieldSetter {
        IntSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setInteger(object, value.asInteger());
        }
    }

    public static class LongSetter extends JsonFieldSetter {
        LongSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setLong(object, value.asLong());
        }
    }

    public static class FloatSetter extends JsonFieldSetter {
        FloatSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setFloat(object, value.asFloat());
        }
    }

    public static class DoubleSetter extends JsonFieldSetter {
        DoubleSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setDouble(object, value.asDouble());
        }
    }

    public static class CharSetter extends JsonFieldSetter {
        CharSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setChar(object, value.asChar());
        }
    }

    public static class BooleanSetter extends JsonFieldSetter {
        BooleanSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setBoolean(object, value.asBoolean());
        }
    }

    public static class ObjectSetter extends JsonFieldSetter {
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
        EnumSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            Class cl = field.getType();
            Object ob = value == null ? null : value.asEnum(cl);
            setObject(object, ob);
        }
    }

    public static class BooleanBoxedSetter extends BooleanSetter {
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
        FloatBoxedSetter(Field f) {
            super(f);
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
        DoubleBoxedSetter(Field f) {
            super(f);
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
