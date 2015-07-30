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

    public FieldReflection create(Class clazz, String name, boolean setAccessible) throws NoSuchFieldException {
        return create(clazz.getDeclaredField(name), setAccessible);
    }

    public FieldReflection create(Class clazz, String name) throws NoSuchFieldException {
        return create(clazz.getDeclaredField(name), false);
    }

    public FieldReflection create(Field field) {
        return create(field, false);
    }

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

    protected Type getType(Field field) {
        Class cl = field.getType();
        Type type;
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
        return type;
    }
}
