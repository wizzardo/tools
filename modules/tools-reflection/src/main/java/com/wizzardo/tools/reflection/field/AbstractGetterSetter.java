package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.reflection.FieldReflection;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public abstract class AbstractGetterSetter extends FieldReflection {

    protected AbstractGetterSetter() {
    }

    public AbstractGetterSetter(Field field) {
        super(field);
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
