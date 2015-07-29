package com.wizzardo.tools.reflection;

import com.wizzardo.tools.reflection.field.*;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by wizzardo on 29.07.15.
 */
public class FieldReflectionFactory {

    protected static final Unsafe unsafe = UnsafeTools.getUnsafe();

    public FieldReflection create(Field field, boolean setAccessible) {
        if (setAccessible)
            field.setAccessible(true);


        if (unsafe != null && (field.getModifiers() & Modifier.STATIC) == 0)
            return createUnsafe(field);
        else
            return createReflection(field);
    }

    protected FieldReflection createReflection(Field field) {
        switch (getType(field)) {
            case BOOLEAN:
                return createBooleanReflectionGetterSetter(field);
            case BYTE:
                return createByteReflectionGetterSetter(field);
            case CHAR:
                return createCharReflectionGetterSetter(field);
            case DOUBLE:
                return createDoubleReflectionGetterSetter(field);
            case FLOAT:
                return createFloatReflectionGetterSetter(field);
            case INTEGER:
                return createIntegerReflectionGetterSetter(field);
            case LONG:
                return createLongReflectionGetterSetter(field);
            case OBJECT:
                return createObjectReflectionGetterSetter(field);
            case SHORT:
                return createShortReflectionGetterSetter(field);
        }
        return null;
    }

    protected FieldReflection createShortReflectionGetterSetter(Field field) {
        return new ShortReflectionGetterSetter(field);
    }

    protected FieldReflection createObjectReflectionGetterSetter(Field field) {
        return new ObjectReflectionGetterSetter(field);
    }

    protected FieldReflection createLongReflectionGetterSetter(Field field) {
        return new LongReflectionGetterSetter(field);
    }

    protected FieldReflection createIntegerReflectionGetterSetter(Field field) {
        return new IntegerReflectionGetterSetter(field);
    }

    protected FieldReflection createFloatReflectionGetterSetter(Field field) {
        return new FloatReflectionGetterSetter(field);
    }

    protected FieldReflection createDoubleReflectionGetterSetter(Field field) {
        return new DoubleReflectionGetterSetter(field);
    }

    protected FieldReflection createCharReflectionGetterSetter(Field field) {
        return new CharReflectionGetterSetter(field);
    }

    protected FieldReflection createByteReflectionGetterSetter(Field field) {
        return new ByteReflectionGetterSetter(field);
    }

    protected FieldReflection createBooleanReflectionGetterSetter(Field field) {
        return new BooleanReflectionGetterSetter(field);
    }

    protected FieldReflection createUnsafe(Field field) {
        switch (getType(field)) {
            case BOOLEAN:
                return createBooleanUnsafeGetterSetter(field);
            case BYTE:
                return createByteUnsafeGetterSetter(field);
            case CHAR:
                return createCharUnsafeGetterSetter(field);
            case DOUBLE:
                return createDoubleUnsafeGetterSetter(field);
            case FLOAT:
                return createFloatUnsafeGetterSetter(field);
            case INTEGER:
                return createIntegerUnsafeGetterSetter(field);
            case LONG:
                return createLongUnsafeGetterSetter(field);
            case OBJECT:
                return createObjectUnsafeGetterSetter(field);
            case SHORT:
                return createShortUnsafeGetterSetter(field);
        }
        return null;
    }

    protected FieldReflection createShortUnsafeGetterSetter(Field field) {
        return new ShortUnsafeGetterSetter(field);
    }

    protected FieldReflection createObjectUnsafeGetterSetter(Field field) {
        return new ObjectUnsafeGetterSetter(field);
    }

    protected FieldReflection createLongUnsafeGetterSetter(Field field) {
        return new LongUnsafeGetterSetter(field);
    }

    protected FieldReflection createIntegerUnsafeGetterSetter(Field field) {
        return new IntegerUnsafeGetterSetter(field);
    }

    protected FieldReflection createFloatUnsafeGetterSetter(Field field) {
        return new FloatUnsafeGetterSetter(field);
    }

    protected FieldReflection createDoubleUnsafeGetterSetter(Field field) {
        return new DoubleUnsafeGetterSetter(field);
    }

    protected FieldReflection createCharUnsafeGetterSetter(Field field) {
        return new CharUnsafeGetterSetter(field);
    }

    protected FieldReflection createByteUnsafeGetterSetter(Field field) {
        return new ByteUnsafeGetterSetter(field);
    }

    protected FieldReflection createBooleanUnsafeGetterSetter(Field field) {
        return new BooleanUnsafeGetterSetter(field);
    }

    protected FieldReflection.Type getType(Field field) {
        Class cl = field.getType();
        FieldReflection.Type type;
        if (cl == int.class)
            type = FieldReflection.Type.INTEGER;
        else if (cl == long.class)
            type = FieldReflection.Type.LONG;
        else if (cl == byte.class)
            type = FieldReflection.Type.BYTE;
        else if (cl == short.class)
            type = FieldReflection.Type.SHORT;
        else if (cl == float.class)
            type = FieldReflection.Type.FLOAT;
        else if (cl == double.class)
            type = FieldReflection.Type.DOUBLE;
        else if (cl == char.class)
            type = FieldReflection.Type.CHAR;
        else if (cl == boolean.class)
            type = FieldReflection.Type.BOOLEAN;
        else
            type = FieldReflection.Type.OBJECT;
        return type;
    }
}
