package com.wizzardo.tools.reflection;

import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.reflection.field.Type;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public interface FieldReflection {

    void setInteger(Object object, int value);

    void setLong(Object object, long value);

    void setByte(Object object, byte value);

    void setShort(Object object, short value);

    void setFloat(Object object, float value);

    void setDouble(Object object, double value);

    void setChar(Object object, char value);

    void setBoolean(Object object, boolean value);

    void setObject(Object object, Object value);

    int getInteger(Object object);

    long getLong(Object object);

    byte getByte(Object object);

    short getShort(Object object);

    float getFloat(Object object);

    double getDouble(Object object);

    char getChar(Object object);

    boolean getBoolean(Object object);

    Object getObject(Object object);

    Type getType();
}
