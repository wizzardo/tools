package com.wizzardo.tools.reflection;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public class FieldSetter {
    protected Field field;
    protected Unsafe unsafe = UnsafeTools.getUnsafe();
    protected final long offset;
    protected final Type type;

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

    public void setInteger(Object object, int value) {
        if (offset != 0)
            unsafe.putInt(object, offset, value);
        else
            try {
                field.setInt(object, value);
            } catch (IllegalAccessException ignored) {
            }
    }

    public void setLong(Object object, long value) {
        if (offset != 0)
            unsafe.putLong(object, offset, value);
        else
            try {
                field.setLong(object, value);
            } catch (IllegalAccessException ignored) {
            }
    }

    public void setByte(Object object, byte value) {
        if (offset != 0)
            unsafe.putByte(object, offset, value);
        else
            try {
                field.setByte(object, value);
            } catch (IllegalAccessException ignored) {
            }
    }

    public void setShort(Object object, short value) {
        if (offset != 0)
            unsafe.putShort(object, offset, value);
        else
            try {
                field.setShort(object, value);
            } catch (IllegalAccessException ignored) {
            }
    }

    public void setFloat(Object object, float value) {
        if (offset != 0)
            unsafe.putFloat(object, offset, value);
        else
            try {
                field.setFloat(object, value);
            } catch (IllegalAccessException ignored) {
            }
    }

    public void setDouble(Object object, double value) {
        if (offset != 0)
            unsafe.putDouble(object, offset, value);
        else
            try {
                field.setDouble(object, value);
            } catch (IllegalAccessException ignored) {
            }
    }

    public void setChar(Object object, char value) {
        if (offset != 0)
            unsafe.putChar(object, offset, value);
        else
            try {
                field.setChar(object, value);
            } catch (IllegalAccessException ignored) {
            }
    }

    public void setBooalen(Object object, boolean value) {
        if (offset != 0)
            unsafe.putBoolean(object, offset, value);
        else
            try {
                field.setBoolean(object, value);
            } catch (IllegalAccessException ignored) {
            }
    }

    public void setObject(Object object, Object value) {
        if (offset != 0)
            unsafe.putObject(object, offset, value);
        else
            try {
                field.set(object, value);
            } catch (IllegalAccessException ignored) {
            }
    }

    public FieldSetter(Field field) {
        this.field = field;
        if (unsafe != null)
            offset = unsafe.objectFieldOffset(field);
        else
            offset = 0;

        Class cl = field.getType();
        if (cl == int.class)
            type = Type.INTEGER;
        else if (cl == long.class)
            type = Type.LONG;
        else if (cl == byte.class)
            type = Type.BYTE;
        else if (cl == short.class)
            type = Type.SHORT;
        else if (cl == float.class)
            type = Type.FLOAT;
        else if (cl == double.class)
            type = Type.DOUBLE;
        else if (cl == char.class)
            type = Type.CHAR;
        else if (cl == boolean.class)
            type = Type.BOOLEAN;
        else
            type = Type.OBJECT;
    }

    public Type getType() {
        return type;
    }
}
