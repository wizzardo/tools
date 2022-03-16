package com.wizzardo.tools.misc;

import com.wizzardo.tools.reflection.field.Type;

import java.util.Date;

/**
 * Created by wizzardo on 02.03.15.
 */
public abstract class StringConverter<T> {

    public final Type type;

    public static final StringConverter TO_INTEGER = new StringConverter<Integer>(Type.INTEGER) {
        @Override
        public Integer convert(String s) {
            return toInteger(s);
        }
    };

    public static final StringConverter TO_LONG = new StringConverter<Long>(Type.LONG) {
        @Override
        public Long convert(String s) {
            return toLong(s);
        }
    };

    public static final StringConverter TO_BOOLEAN = new StringConverter<Boolean>(Type.BOOLEAN) {
        @Override
        public Boolean convert(String s) {
            return toBoolean(s);
        }
    };

    public static final StringConverter TO_SHORT = new StringConverter<Short>(Type.SHORT) {
        @Override
        public Short convert(String s) {
            return toShort(s);
        }
    };

    public static final StringConverter TO_BYTE = new StringConverter<Byte>(Type.BYTE) {
        @Override
        public Byte convert(String s) {
            return toByte(s);
        }
    };

    public static final StringConverter TO_FLOAT = new StringConverter<Float>(Type.FLOAT) {
        @Override
        public Float convert(String s) {
            return toFloat(s);
        }
    };

    public static final StringConverter TO_DOUBLE = new StringConverter<Double>(Type.DOUBLE) {
        @Override
        public Double convert(String s) {
            return toDouble(s);
        }
    };

    public static final StringConverter TO_DATE = new StringConverter<Date>(Type.OBJECT) {
        @Override
        public Date convert(String s) {
            return toDate(s);
        }
    };

    public static final StringConverter TO_STRING = new StringConverter<String>(Type.OBJECT) {
        @Override
        public String convert(String s) {
            return s;
        }
    };

    public static final StringConverter TO_CHARACTER = new StringConverter<Character>(Type.CHAR) {
        @Override
        public Character convert(String s) {
            if (s.length() > 1) {
                return (char) Integer.parseInt(s);
            } else
                return s.charAt(0);
        }
    };

    protected StringConverter(Type type) {
        this.type = type;
    }

    public abstract T convert(String s);

    public static StringConverter getConverter(final Class clazz) {
        if (clazz == String.class || clazz == Object.class)
            return TO_STRING;
        if (clazz == Integer.class)
            return TO_INTEGER;
        if (clazz == Long.class)
            return TO_LONG;
        if (clazz == Boolean.class)
            return TO_BOOLEAN;
        if (clazz == Date.class)
            return TO_DATE;
        if (clazz == Float.class)
            return TO_FLOAT;
        if (clazz == Double.class)
            return TO_DOUBLE;
        if (clazz == Short.class)
            return TO_SHORT;
        if (clazz == Byte.class)
            return TO_BYTE;
        if (clazz == Character.class)
            return TO_CHARACTER;
        if (clazz.isEnum())
            return new StringConverter<Enum>(Type.OBJECT) {
                @Override
                public Enum convert(String s) {
                    return s == null ? null : Enum.valueOf(clazz, s);
                }
            };

        return null;
    }

    public static Integer toInteger(String s) {
        return Integer.valueOf(s);
    }

    public static Long toLong(String s) {
        return Long.valueOf(s);
    }

    public static Byte toByte(String s) {
        return Byte.valueOf(s);
    }

    public static Short toShort(String s) {
        return Short.valueOf(s);
    }

    public static Boolean toBoolean(String s) {
        return Boolean.valueOf(s);
    }

    public static Float toFloat(String s) {
        return Float.valueOf(s);
    }

    public static Double toDouble(String s) {
        return Double.valueOf(s);
    }

    public static Date toDate(String s) {
        return new DateIso8601().parse(s);
    }
}
