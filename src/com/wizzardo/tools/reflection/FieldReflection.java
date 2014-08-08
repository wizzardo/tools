package com.wizzardo.tools.reflection;

import com.wizzardo.tools.misc.WrappedException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public class FieldReflection {
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
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setLong(Object object, long value) {
        if (offset != 0)
            unsafe.putLong(object, offset, value);
        else
            try {
                field.setLong(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setByte(Object object, byte value) {
        if (offset != 0)
            unsafe.putByte(object, offset, value);
        else
            try {
                field.setByte(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setShort(Object object, short value) {
        if (offset != 0)
            unsafe.putShort(object, offset, value);
        else
            try {
                field.setShort(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setFloat(Object object, float value) {
        if (offset != 0)
            unsafe.putFloat(object, offset, value);
        else
            try {
                field.setFloat(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setDouble(Object object, double value) {
        if (offset != 0)
            unsafe.putDouble(object, offset, value);
        else
            try {
                field.setDouble(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setChar(Object object, char value) {
        if (offset != 0)
            unsafe.putChar(object, offset, value);
        else
            try {
                field.setChar(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setBoolean(Object object, boolean value) {
        if (offset != 0)
            unsafe.putBoolean(object, offset, value);
        else
            try {
                field.setBoolean(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setObject(Object object, Object value) {
        if (offset != 0)
            unsafe.putObject(object, offset, value);
        else
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public int getInteger(Object object) {
        if (offset != 0)
            return unsafe.getInt(object, offset);
        else
            try {
                return field.getInt(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public long getLong(Object object) {
        if (offset != 0)
            return unsafe.getLong(object, offset);
        else
            try {
                return field.getLong(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public byte getByte(Object object) {
        if (offset != 0)
            return unsafe.getByte(object, offset);
        else
            try {
                return field.getByte(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public short getShort(Object object) {
        if (offset != 0)
            return unsafe.getShort(object, offset);
        else
            try {
                return field.getShort(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public float getFloat(Object object) {
        if (offset != 0)
            return unsafe.getFloat(object, offset);
        else
            try {
                return field.getFloat(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public double getDouble(Object object) {
        if (offset != 0)
            return unsafe.getDouble(object, offset);
        else
            try {
                return field.getDouble(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public char setChar(Object object) {
        if (offset != 0)
            return unsafe.getChar(object, offset);
        else
            try {
                return field.getChar(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public boolean getBoolean(Object object) {
        if (offset != 0)
            return unsafe.getBoolean(object, offset);
        else
            try {
                return field.getBoolean(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public Object getObject(Object object) {
        if (offset != 0)
            return unsafe.getObject(object, offset);
        else
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public FieldReflection(Field field) {
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
