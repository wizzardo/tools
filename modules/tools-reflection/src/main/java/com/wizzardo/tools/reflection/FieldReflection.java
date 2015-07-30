package com.wizzardo.tools.reflection;

import com.wizzardo.tools.misc.Unchecked;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public abstract class FieldReflection {
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

    @Override
    public String toString() {
        return field.toString();
    }

    public abstract void setInteger(Object object, int value);

    public abstract void setLong(Object object, long value);

    public abstract void setByte(Object object, byte value);

    public abstract void setShort(Object object, short value);

    public abstract void setFloat(Object object, float value);

    public abstract void setDouble(Object object, double value);

    public abstract void setChar(Object object, char value);

    public abstract void setBoolean(Object object, boolean value);

    public abstract void setObject(Object object, Object value);

    public abstract int getInteger(Object object);

    public abstract long getLong(Object object);

    public abstract byte getByte(Object object);

    public abstract short getShort(Object object);

    public abstract float getFloat(Object object);

    public abstract double getDouble(Object object);

    public abstract char getChar(Object object);

    public abstract boolean getBoolean(Object object);

    public abstract Object getObject(Object object);

    protected FieldReflection() {
        offset = 0;
        type = Type.OBJECT;
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

    public Field getField() {
        return field;
    }
}
