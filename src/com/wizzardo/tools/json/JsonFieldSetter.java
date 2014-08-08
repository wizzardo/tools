package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldReflection;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
abstract class JsonFieldSetter extends FieldReflection {

    protected JsonFieldSetter(Field f) {
        super(f);
    }

    public static JsonFieldSetter createSetter(Field f) {
        Class cl = f.getType();
        if (cl == int.class)
            return new IntSetter(f);

        if (cl == long.class)
            return new LongSetter(f);

        if (cl == byte.class)
            return new ByteSetter(f);

        if (cl == short.class)
            return new ShortSetter(f);

        if (cl == float.class)
            return new FloatSetter(f);

        if (cl == double.class)
            return new DoubleSetter(f);

        if (cl == char.class)
            return new CharSetter(f);

        if (cl == boolean.class)
            return new BooleanSetter(f);

        if (cl.isEnum())
            return new EnumSetter(f);

        return new ObjectSetter(f);
    }

    abstract void set(Object object, JsonItem value);

    public static class ByteSetter extends JsonFieldSetter {

        ByteSetter(Field f) {
            super(f);
        }

        public void set(Object object, JsonItem value) {
            setByte(object, value.asByte((byte) 0));
        }
    }

    public static class ShortSetter extends JsonFieldSetter {
        ShortSetter(Field f) {
            super(f);
        }

        public void set(Object object, JsonItem value) {
            setShort(object, value.asShort((short) 0));
        }
    }

    public static class IntSetter extends JsonFieldSetter {
        IntSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setInteger(object, value.asInteger(0));
        }
    }

    public static class LongSetter extends JsonFieldSetter {
        LongSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setLong(object, value.asLong(0l));
        }
    }

    public static class FloatSetter extends JsonFieldSetter {
        FloatSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setFloat(object, value.asFloat(0f));
        }
    }

    public static class DoubleSetter extends JsonFieldSetter {
        DoubleSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setDouble(object, value.asDouble(0d));
        }
    }

    public static class CharSetter extends JsonFieldSetter {
        CharSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setChar(object, (char) (int) value.asInteger(0));
        }
    }

    public static class BooleanSetter extends JsonFieldSetter {
        BooleanSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            setBoolean(object, value.asBoolean(Boolean.FALSE));
        }
    }

    public static class ObjectSetter extends JsonFieldSetter {
        ObjectSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            Object ob = value == null ? null : value.getAs(field.getType());
            setObject(object, ob);
        }
    }

    public static class EnumSetter extends JsonFieldSetter {
        EnumSetter(Field f) {
            super(f);
        }

        @Override
        public void set(Object object, JsonItem value) {
            Class cl = field.getType();
            Object ob = value == null ? null : value.asEnum(cl);
            setObject(object, ob);
        }
    }
}
