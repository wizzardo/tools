package com.wizzardo.tools.reflection;

import com.wizzardo.tools.misc.WrappedException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public class FieldReflection {
    protected static final Unsafe unsafe = UnsafeTools.getUnsafe();
    protected Field field;
    protected final long offset;
    protected final Type type;

    protected static final boolean putInt = hasMethod(Unsafe.class, "putInt", Object.class, long.class, int.class);
    protected static final boolean putLong = hasMethod(Unsafe.class, "putLong", Object.class, long.class, long.class);
    protected static final boolean putByte = hasMethod(Unsafe.class, "putByte", Object.class, long.class, byte.class);
    protected static final boolean putShort = hasMethod(Unsafe.class, "putShort", Object.class, long.class, short.class);
    protected static final boolean putFloat = hasMethod(Unsafe.class, "putFloat", Object.class, long.class, float.class);
    protected static final boolean putDouble = hasMethod(Unsafe.class, "putDouble", Object.class, long.class, double.class);
    protected static final boolean putChar = hasMethod(Unsafe.class, "putChar", Object.class, long.class, char.class);
    protected static final boolean putBoolean = hasMethod(Unsafe.class, "putBoolean", Object.class, long.class, boolean.class);
    protected static final boolean putObject = hasMethod(Unsafe.class, "putObject", Object.class, long.class, Object.class);

    protected static final boolean getInt = hasMethod(Unsafe.class, "getInt", Object.class, long.class);
    protected static final boolean getLong = hasMethod(Unsafe.class, "getLong", Object.class, long.class);
    protected static final boolean getByte = hasMethod(Unsafe.class, "getByte", Object.class, long.class);
    protected static final boolean getShort = hasMethod(Unsafe.class, "getShort", Object.class, long.class);
    protected static final boolean getFloat = hasMethod(Unsafe.class, "getFloat", Object.class, long.class);
    protected static final boolean getDouble = hasMethod(Unsafe.class, "getDouble", Object.class, long.class);
    protected static final boolean getChar = hasMethod(Unsafe.class, "getChar", Object.class, long.class);
    protected static final boolean getBoolean = hasMethod(Unsafe.class, "getBoolean", Object.class, long.class);
    protected static final boolean getObject = hasMethod(Unsafe.class, "getObject", Object.class, long.class);

    private static Boolean hasMethod(Class clazz, String name, Class... args) {
        try {
            clazz.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

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
        if (putInt && offset != 0)
            unsafe.putInt(object, offset, value);
        else
            try {
                field.setInt(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setLong(Object object, long value) {
        if (putLong && offset != 0)
            unsafe.putLong(object, offset, value);
        else
            try {
                field.setLong(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setByte(Object object, byte value) {
        if (putByte && offset != 0)
            unsafe.putByte(object, offset, value);
        else
            try {
                field.setByte(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setShort(Object object, short value) {
        if (putShort && offset != 0)
            unsafe.putShort(object, offset, value);
        else
            try {
                field.setShort(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setFloat(Object object, float value) {
        if (putFloat && offset != 0)
            unsafe.putFloat(object, offset, value);
        else
            try {
                field.setFloat(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setDouble(Object object, double value) {
        if (putDouble && offset != 0)
            unsafe.putDouble(object, offset, value);
        else
            try {
                field.setDouble(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setChar(Object object, char value) {
        if (putChar && offset != 0)
            unsafe.putChar(object, offset, value);
        else
            try {
                field.setChar(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setBoolean(Object object, boolean value) {
        if (putBoolean && offset != 0)
            unsafe.putBoolean(object, offset, value);
        else
            try {
                field.setBoolean(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public void setObject(Object object, Object value) {
        if (putObject && offset != 0)
            unsafe.putObject(object, offset, value);
        else
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public int getInteger(Object object) {
        if (getInt && offset != 0)
            return unsafe.getInt(object, offset);
        else
            try {
                return field.getInt(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public long getLong(Object object) {
        if (getLong && offset != 0)
            return unsafe.getLong(object, offset);
        else
            try {
                return field.getLong(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public byte getByte(Object object) {
        if (getByte && offset != 0)
            return unsafe.getByte(object, offset);
        else
            try {
                return field.getByte(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public short getShort(Object object) {
        if (getShort && offset != 0)
            return unsafe.getShort(object, offset);
        else
            try {
                return field.getShort(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public float getFloat(Object object) {
        if (getFloat && offset != 0)
            return unsafe.getFloat(object, offset);
        else
            try {
                return field.getFloat(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public double getDouble(Object object) {
        if (getDouble && offset != 0)
            return unsafe.getDouble(object, offset);
        else
            try {
                return field.getDouble(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public char getChar(Object object) {
        if (getChar && offset != 0)
            return unsafe.getChar(object, offset);
        else
            try {
                return field.getChar(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public boolean getBoolean(Object object) {
        if (getBoolean && offset != 0)
            return unsafe.getBoolean(object, offset);
        else
            try {
                return field.getBoolean(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public Object getObject(Object object) {
        if (getObject && offset != 0)
            return unsafe.getObject(object, offset);
        else
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
    }

    public FieldReflection(Class clazz, String name, boolean setAccessible) throws NoSuchFieldException {
        this(clazz.getDeclaredField(name), setAccessible);
    }

    public FieldReflection(Class clazz, String name) throws NoSuchFieldException {
        this(clazz.getDeclaredField(name), false);
    }

    public FieldReflection(Field field) {
        this(field, false);
    }

    public FieldReflection(Field field, boolean setAccessible) {
        this.field = field;
        if (setAccessible)
            field.setAccessible(true);

        if (unsafe != null && (field.getModifiers() & Modifier.STATIC) == 0)
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
