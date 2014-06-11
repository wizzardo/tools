package com.wizzardo.tools.json;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
interface FieldSetter {

    public static class Factory {
        public static FieldSetter createSetter(Field f) {
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
    }

    void doSet(Object object, JsonItem value);

    public com.wizzardo.tools.reflection.FieldSetter.Type getType();

    public static class ByteSetter extends com.wizzardo.tools.reflection.FieldSetter.ByteSetter implements FieldSetter {

        ByteSetter(Field f) {
            super(f);
        }

        public void doSet(Object object, JsonItem value) {
            try {
                set(object, value.asByte((byte) 0));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static class ShortSetter extends com.wizzardo.tools.reflection.FieldSetter.ShortSetter implements FieldSetter {
        ShortSetter(Field f) {
            super(f);
        }

        public void doSet(Object object, JsonItem value) {
            try {
                set(object, value.asShort((short) 0));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static class IntSetter extends com.wizzardo.tools.reflection.FieldSetter.IntSetter implements FieldSetter {
        IntSetter(Field f) {
            super(f);
        }

        @Override
        public void doSet(Object object, JsonItem value) {
            try {
                set(object, value.asInteger(0));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static class LongSetter extends com.wizzardo.tools.reflection.FieldSetter.LongSetter implements FieldSetter {
        LongSetter(Field f) {
            super(f);
        }

        @Override
        public void doSet(Object object, JsonItem value) {
            try {
                set(object, value.asLong(0l));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static class FloatSetter extends com.wizzardo.tools.reflection.FieldSetter.FloatSetter implements FieldSetter {
        FloatSetter(Field f) {
            super(f);
        }

        @Override
        public void doSet(Object object, JsonItem value) {
            try {
                set(object, value.asFloat(0f));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static class DoubleSetter extends com.wizzardo.tools.reflection.FieldSetter.DoubleSetter implements FieldSetter {
        DoubleSetter(Field f) {
            super(f);
        }

        @Override
        public void doSet(Object object, JsonItem value) {
            try {
                set(object, value.asDouble(0d));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static class CharSetter extends com.wizzardo.tools.reflection.FieldSetter.CharSetter implements FieldSetter {
        CharSetter(Field f) {
            super(f);
        }

        @Override
        public void doSet(Object object, JsonItem value) {
            try {
                set(object, (char) (int) value.asInteger(0));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static class BooleanSetter extends com.wizzardo.tools.reflection.FieldSetter.BooleanSetter implements FieldSetter {
        BooleanSetter(Field f) {
            super(f);
        }

        @Override
        public void doSet(Object object, JsonItem value) {
            try {
                set(object, value.asBoolean(Boolean.FALSE));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static class ObjectSetter extends com.wizzardo.tools.reflection.FieldSetter.ObjectSetter implements FieldSetter {
        ObjectSetter(Field f) {
            super(f);
        }

        @Override
        public void doSet(Object object, JsonItem value) {
            Object ob = value == null ? null : value.getAs(f.getType());
            try {
                set(object, ob);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static class EnumSetter extends com.wizzardo.tools.reflection.FieldSetter.ObjectSetter implements FieldSetter {
        EnumSetter(Field f) {
            super(f);
        }

        @Override
        public void doSet(Object object, JsonItem value) {
            Class cl = f.getType();
            Object ob = value == null ? null : value.asEnum(cl);
            try {
                set(object, ob);
            } catch (IllegalAccessException ignored) {
            }
        }
    }
}
