package com.wizzardo.tools.reflection;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public class FieldSetter {
    protected Field f;
    protected Unsafe unsafe = UnsafeTools.getUnsafe();
    protected long offset;

    public static enum Type {
        INTEGER,
        LONG,
        BYTE,
        SHORT,
        FLOAT,
        DOUBLE,
        CHAR,
        BOOLEAN,
        OBJECT
    }

    protected FieldSetter(Field f) {
        this.f = f;
        if (unsafe != null)
            offset = unsafe.objectFieldOffset(f);
    }

    public Type getType() {
        return Type.OBJECT;
    }

    public static FieldSetter getSetter(Field f) {
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

        return new ObjectSetter(f);
    }


    public static class ByteSetter extends FieldSetter {

        public ByteSetter(Field f) {
            super(f);
        }

        public void set(Object object, byte b) throws IllegalAccessException {
            if (offset != 0)
                unsafe.putByte(object, offset, b);
            else
                f.setByte(object, b);
        }

        @Override
        public Type getType() {
            return Type.BYTE;
        }
    }

    public static class ShortSetter extends FieldSetter {
        public ShortSetter(Field f) {
            super(f);
        }

        public void set(Object object, short s) throws IllegalAccessException {
            if (offset != 0)
                unsafe.putShort(object, offset, s);
            else
                f.setShort(object, s);
        }

        @Override
        public Type getType() {
            return Type.SHORT;
        }
    }

    public static class IntSetter extends FieldSetter {
        public IntSetter(Field f) {
            super(f);
        }

        public void set(Object object, int i) throws IllegalAccessException {
            if (offset != 0)
                unsafe.putInt(object, offset, i);
            else
                f.setInt(object, i);
        }

        @Override
        public Type getType() {
            return Type.INTEGER;
        }
    }

    public static class LongSetter extends FieldSetter {
        public LongSetter(Field f) {
            super(f);
        }

        public void set(Object object, long l) throws IllegalAccessException {
            if (offset != 0)
                unsafe.putLong(object, offset, l);
            else
                f.setLong(object, l);
        }

        @Override
        public Type getType() {
            return Type.LONG;
        }
    }

    public static class FloatSetter extends FieldSetter {
        public FloatSetter(Field f) {
            super(f);
        }

        public void set(Object object, float fl) throws IllegalAccessException {
            if (offset != 0)
                unsafe.putFloat(object, offset, fl);
            else
                f.setFloat(object, fl);
        }

        @Override
        public Type getType() {
            return Type.FLOAT;
        }
    }

    public static class DoubleSetter extends FieldSetter {
        public DoubleSetter(Field f) {
            super(f);
        }

        public void set(Object object, double d) throws IllegalAccessException {
            if (offset != 0)
                unsafe.putDouble(object, offset, d);
            else
                f.setDouble(object, d);
        }

        @Override
        public Type getType() {
            return Type.DOUBLE;
        }
    }

    public static class CharSetter extends FieldSetter {
        public CharSetter(Field f) {
            super(f);
        }

        public void set(Object object, char c) throws IllegalAccessException {
            if (offset != 0)
                unsafe.putChar(object, offset, c);
            else
                f.setChar(object, c);
        }

        @Override
        public Type getType() {
            return Type.CHAR;
        }
    }

    public static class BooleanSetter extends FieldSetter {
        public BooleanSetter(Field f) {
            super(f);
        }

        public void set(Object object, boolean b) throws IllegalAccessException {
            if (offset != 0)
                unsafe.putBoolean(object, offset, b);
            else
                f.setBoolean(object, b);
        }

        @Override
        public Type getType() {
            return Type.BOOLEAN;
        }
    }

    public static class ObjectSetter extends FieldSetter {
        public ObjectSetter(Field f) {
            super(f);
        }

        public void set(Object object, Object ob) throws IllegalAccessException {
            if (offset != 0)
                unsafe.putObject(object, offset, ob);
            else
                f.set(object, ob);
        }
    }
}
