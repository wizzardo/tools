package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.FieldReflectionFactory;
import com.wizzardo.tools.reflection.field.*;
import com.wizzardo.tools.reflection.field.Type;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public class JsonFieldSetterFactory extends FieldReflectionFactory {

    @Override
    public JsonFieldSetter create(Class clazz, String name, boolean setAccessible) throws NoSuchFieldException {
        return (JsonFieldSetter) super.create(clazz, name, setAccessible);
    }

    @Override
    public JsonFieldSetter create(Class clazz, String name) throws NoSuchFieldException {
        return (JsonFieldSetter) super.create(clazz, name);
    }

    @Override
    public JsonFieldSetter create(Field field, boolean setAccessible) {
        return (JsonFieldSetter) super.create(field, setAccessible);
    }

    @Override
    public JsonFieldSetter create(Field f) {
        return create(f, f.getType());
    }

    public JsonFieldSetter create(Field f, Class cl) {
        if (f.getType() != cl && f.getType() != Object.class)
            throw new IllegalArgumentException();

        boolean b = isUnsafeAvailable(f) && getObject && putObject;
        if (b) {
            if (cl.isEnum())
                return new UnsafeEnumSetter(f);

            if (cl == Boolean.class)
                return new UnsafeBoxedSetter(f, StringConverter.TO_BOOLEAN);

            if (cl == Integer.class)
                return new UnsafeBoxedSetter(f, StringConverter.TO_INTEGER);

            if (cl == Long.class)
                return new UnsafeBoxedSetter(f, StringConverter.TO_LONG);

            if (cl == Byte.class)
                return new UnsafeBoxedSetter(f, StringConverter.TO_BYTE);

            if (cl == Short.class)
                return new UnsafeBoxedSetter(f, StringConverter.TO_SHORT);

            if (cl == Character.class)
                return new UnsafeBoxedSetter(f, StringConverter.TO_CHARACTER);

            if (cl == Float.class)
                return new UnsafeBoxedSetter(f, StringConverter.TO_FLOAT);

            if (cl == Double.class)
                return new UnsafeBoxedSetter(f, StringConverter.TO_DOUBLE);

            if (cl == Date.class)
                return new UnsafeBoxedSetter(f, StringConverter.TO_DATE);
        } else {
            if (cl.isEnum())
                return new ReflectionEnumSetter(f);

            if (cl == Boolean.class)
                return new ReflectionBoxedSetter(f, StringConverter.TO_BOOLEAN);

            if (cl == Integer.class)
                return new ReflectionBoxedSetter(f, StringConverter.TO_INTEGER);

            if (cl == Long.class)
                return new ReflectionBoxedSetter(f, StringConverter.TO_LONG);

            if (cl == Byte.class)
                return new ReflectionBoxedSetter(f, StringConverter.TO_BYTE);

            if (cl == Short.class)
                return new ReflectionBoxedSetter(f, StringConverter.TO_SHORT);

            if (cl == Character.class)
                return new ReflectionBoxedSetter(f, StringConverter.TO_CHARACTER);

            if (cl == Float.class)
                return new ReflectionBoxedSetter(f, StringConverter.TO_FLOAT);

            if (cl == Double.class)
                return new ReflectionBoxedSetter(f, StringConverter.TO_DOUBLE);

            if (cl == Date.class)
                return new ReflectionBoxedSetter(f, StringConverter.TO_DATE);
        }
        return (JsonFieldSetter) super.create(f);
    }

    @Override
    protected FieldReflection createShortReflectionGetterSetter(Field field) {
        return new ReflectionShortSetter(field);
    }

    @Override
    protected FieldReflection createObjectReflectionGetterSetter(Field field) {
        return new ReflectionObjectSetter(field);
    }

    @Override
    protected FieldReflection createLongReflectionGetterSetter(Field field) {
        return new ReflectionLongSetter(field);
    }

    @Override
    protected FieldReflection createIntegerReflectionGetterSetter(Field field) {
        return new ReflectionIntSetter(field);
    }

    @Override
    protected FieldReflection createFloatReflectionGetterSetter(Field field) {
        return new ReflectionFloatSetter(field);
    }

    @Override
    protected FieldReflection createDoubleReflectionGetterSetter(Field field) {
        return new ReflectionDoubleSetter(field);
    }

    @Override
    protected FieldReflection createCharReflectionGetterSetter(Field field) {
        return new ReflectionCharSetter(field);
    }

    @Override
    protected FieldReflection createByteReflectionGetterSetter(Field field) {
        return new ReflectionByteSetter(field);
    }

    @Override
    protected FieldReflection createBooleanReflectionGetterSetter(Field field) {
        return new ReflectionBooleanSetter(field);
    }

    @Override
    protected FieldReflection createShortUnsafeGetterSetter(Field field) {
        return new UnsafeShortSetter(field);
    }

    @Override
    protected FieldReflection createObjectUnsafeGetterSetter(Field field) {
        return new UnsafeObjectSetter(field);
    }

    @Override
    protected FieldReflection createLongUnsafeGetterSetter(Field field) {
        return new UnsafeLongSetter(field);
    }

    @Override
    protected FieldReflection createIntegerUnsafeGetterSetter(Field field) {
        return new UnsafeIntSetter(field);
    }

    @Override
    protected FieldReflection createFloatUnsafeGetterSetter(Field field) {
        return new UnsafeFloatSetter(field);
    }

    @Override
    protected FieldReflection createDoubleUnsafeGetterSetter(Field field) {
        return new UnsafeDoubleSetter(field);
    }

    @Override
    protected FieldReflection createCharUnsafeGetterSetter(Field field) {
        return new UnsafeCharSetter(field);
    }

    @Override
    protected FieldReflection createByteUnsafeGetterSetter(Field field) {
        return new UnsafeByteSetter(field);
    }

    @Override
    protected FieldReflection createBooleanUnsafeGetterSetter(Field field) {
        return new UnsafeBooleanSetter(field);
    }

    public static class ReflectionEnumSetter extends ObjectReflectionGetterSetter implements JsonFieldSetter {

        ReflectionEnumSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            Class cl = field.getType();
            setObject(object, JsonUtils.asEnum(cl, value));
        }
    }

    public static class ReflectionBoxedSetter extends ObjectReflectionGetterSetter implements JsonFieldSetter {
        private final StringConverter converter;

        ReflectionBoxedSetter(Field f, StringConverter converter) {
            super(f);
            this.converter = converter;
        }

        @Override
        public void setString(Object object, String value) {
            setObject(object, converter.convert(value));
        }

        @Override
        public void setInteger(Object object, int value) {
            setObject(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setObject(object, value);
        }

        @Override
        public void setByte(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setObject(object, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setObject(object, value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setObject(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setObject(object, value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setObject(object, value);
        }

        @Override
        public Type getType() {
            return converter.type;
        }
    }

    public static class UnsafeEnumSetter extends ObjectUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeEnumSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            Class cl = field.getType();
            setObject(object, JsonUtils.asEnum(cl, value));
        }
    }

    public static class UnsafeBoxedSetter extends ObjectUnsafeGetterSetter implements JsonFieldSetter {
        private final StringConverter converter;

        UnsafeBoxedSetter(Field f, StringConverter converter) {
            super(f);
            this.converter = converter;
        }

        @Override
        public void setString(Object object, String value) {
            setObject(object, converter.convert(value));
        }

        @Override
        public void setInteger(Object object, int value) {
            setObject(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setObject(object, value);
        }

        @Override
        public void setByte(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setObject(object, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setObject(object, value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setObject(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setObject(object, value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setObject(object, value);
        }

        @Override
        public Type getType() {
            return converter.type;
        }
    }

    public static class ReflectionByteSetter extends ByteReflectionGetterSetter implements JsonFieldSetter {

        ReflectionByteSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setByte(object, Byte.parseByte(value));
        }
    }

    public static class ReflectionShortSetter extends ShortReflectionGetterSetter implements JsonFieldSetter {

        ReflectionShortSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setShort(object, Short.parseShort(value));
        }
    }

    public static class ReflectionObjectSetter extends ObjectReflectionGetterSetter implements JsonFieldSetter {

        ReflectionObjectSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setObject(object, value);
        }

        @Override
        public void setInteger(Object object, int value) {
            setObject(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setObject(object, value);
        }

        @Override
        public void setByte(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setObject(object, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setObject(object, value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setObject(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setObject(object, value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setObject(object, value);
        }
    }

    public static class ReflectionIntSetter extends IntegerReflectionGetterSetter implements JsonFieldSetter {

        ReflectionIntSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setInteger(object, Integer.parseInt(value));
        }
    }

    public static class ReflectionLongSetter extends LongReflectionGetterSetter implements JsonFieldSetter {

        ReflectionLongSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setLong(object, Long.parseLong(value));
        }
    }

    public static class ReflectionFloatSetter extends FloatReflectionGetterSetter implements JsonFieldSetter {

        ReflectionFloatSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setFloat(object, Float.parseFloat(value));
        }
    }

    public static class ReflectionDoubleSetter extends DoubleReflectionGetterSetter implements JsonFieldSetter {

        ReflectionDoubleSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setDouble(object, Double.parseDouble(value));
        }
    }

    public static class ReflectionCharSetter extends CharReflectionGetterSetter implements JsonFieldSetter {

        ReflectionCharSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            if (value.length() > 1) {
                setChar(object, (char) Integer.parseInt(value));
            } else
                setChar(object, value.charAt(0));
        }
    }

    public static class ReflectionBooleanSetter extends BooleanReflectionGetterSetter implements JsonFieldSetter {

        ReflectionBooleanSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setBoolean(object, Boolean.parseBoolean(value));
        }
    }

    public static class UnsafeByteSetter extends ByteUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeByteSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setByte(object, Byte.parseByte(value));
        }
    }

    public static class UnsafeShortSetter extends ShortUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeShortSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setShort(object, Short.parseShort(value));
        }
    }

    public static class UnsafeObjectSetter extends ObjectUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeObjectSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setObject(object, value);
        }

        @Override
        public void setInteger(Object object, int value) {
            setObject(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setObject(object, value);
        }

        @Override
        public void setByte(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setObject(object, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setObject(object, value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setObject(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setObject(object, value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setObject(object, value);
        }
    }

    public static class UnsafeIntSetter extends IntegerUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeIntSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setInteger(object, Integer.parseInt(value));
        }
    }

    public static class UnsafeLongSetter extends LongUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeLongSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setLong(object, Long.parseLong(value));
        }
    }

    public static class UnsafeFloatSetter extends FloatUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeFloatSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setFloat(object, Float.parseFloat(value));
        }
    }

    public static class UnsafeDoubleSetter extends DoubleUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeDoubleSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setDouble(object, Double.parseDouble(value));
        }
    }

    public static class UnsafeCharSetter extends CharUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeCharSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            if (value.length() > 1) {
                setChar(object, (char) Integer.parseInt(value));
            } else
                setChar(object, value.charAt(0));
        }
    }

    public static class UnsafeBooleanSetter extends BooleanUnsafeGetterSetter implements JsonFieldSetter {

        UnsafeBooleanSetter(Field f) {
            super(f);
        }

        @Override
        public void setString(Object object, String value) {
            setBoolean(object, Boolean.parseBoolean(value));
        }
    }
}
