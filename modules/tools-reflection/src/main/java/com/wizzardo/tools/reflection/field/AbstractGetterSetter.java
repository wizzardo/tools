package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.UnsafeTools;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by wizzardo on 28.07.15.
 */
public abstract class AbstractGetterSetter implements FieldReflection {
    protected static final Unsafe unsafe = UnsafeTools.getUnsafe();
    protected Field field;
    protected final long offset;
    protected final Type type;

    protected AbstractGetterSetter() {
        offset = 0;
        type = null;
    }

    public AbstractGetterSetter(Field field) {
        this(field, false);
    }
    public AbstractGetterSetter(Field field, boolean setAccessible) {
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

    @Override
    public String toString() {
        return field.toString();
    }

    @Override
    public void setInteger(Object object, int value) {
        throw new IllegalStateException("Can not set Integer value to field " + field + ". not implemented");
    }

    @Override
    public void setLong(Object object, long value) {
        throw new IllegalStateException("Can not set Long value to field " + field + ". not implemented");
    }

    @Override
    public void setByte(Object object, byte value) {
        throw new IllegalStateException("Can not set Byte value to field " + field + ". not implemented");
    }

    @Override
    public void setShort(Object object, short value) {
        throw new IllegalStateException("Can not set Short value to field " + field + ". not implemented");
    }

    @Override
    public void setFloat(Object object, float value) {
        throw new IllegalStateException("Can not set Float value to field " + field + ". not implemented");
    }

    @Override
    public void setDouble(Object object, double value) {
        throw new IllegalStateException("Can not set Double value to field " + field + ". not implemented");
    }

    @Override
    public void setChar(Object object, char value) {
        throw new IllegalStateException("Can not set Char value to field " + field + ". not implemented");
    }

    @Override
    public void setBoolean(Object object, boolean value) {
        throw new IllegalStateException("Can not set Boolean value to field " + field + ". not implemented");
    }

    @Override
    public void setObject(Object object, Object value) {
        throw new IllegalStateException("Can not set Object value to field " + field + ". not implemented");
    }

    @Override
    public int getInteger(Object object) {
        throw new IllegalStateException("Can not get Integer value from field " + field + ". not implemented");
    }

    @Override
    public long getLong(Object object) {
        throw new IllegalStateException("Can not get Long value from field " + field + ". not implemented");
    }

    @Override
    public byte getByte(Object object) {
        throw new IllegalStateException("Can not get Byte value from field " + field + ". not implemented");
    }

    @Override
    public short getShort(Object object) {
        throw new IllegalStateException("Can not get Short value from field " + field + ". not implemented");
    }

    @Override
    public float getFloat(Object object) {
        throw new IllegalStateException("Can not get Float value from field " + field + ". not implemented");
    }

    @Override
    public double getDouble(Object object) {
        throw new IllegalStateException("Can not get Double value from field " + field + ". not implemented");
    }

    @Override
    public char getChar(Object object) {
        throw new IllegalStateException("Can not get Char value from field " + field + ". not implemented");
    }

    @Override
    public boolean getBoolean(Object object) {
        throw new IllegalStateException("Can not get Boolean value from field " + field + ". not implemented");
    }

    @Override
    public Object getObject(Object object) {
        throw new IllegalStateException("Can not get Object value from field " + field + ". not implemented");
    }
}
