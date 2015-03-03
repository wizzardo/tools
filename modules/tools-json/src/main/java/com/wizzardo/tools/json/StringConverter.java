package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.DateIso8601;
import com.wizzardo.tools.reflection.FieldReflection;

import java.util.Date;

/**
 * Created by wizzardo on 02.03.15.
 */
abstract class StringConverter<T> {

    final FieldReflection.Type type;

    static final StringConverter TO_INTEGER = new StringConverter<Integer>(FieldReflection.Type.INTEGER) {
        @Override
        Integer convert(String s) {
            return toInteger(s);
        }
    };

    static final StringConverter TO_LONG = new StringConverter<Long>(FieldReflection.Type.LONG) {
        @Override
        Long convert(String s) {
            return toLong(s);
        }
    };

    static final StringConverter TO_BOOLEAN = new StringConverter<Boolean>(FieldReflection.Type.BOOLEAN) {
        @Override
        Boolean convert(String s) {
            return toBoolean(s);
        }
    };

    static final StringConverter TO_SHORT = new StringConverter<Short>(FieldReflection.Type.SHORT) {
        @Override
        Short convert(String s) {
            return toShort(s);
        }
    };

    static final StringConverter TO_BYTE = new StringConverter<Byte>(FieldReflection.Type.BYTE) {
        @Override
        Byte convert(String s) {
            return toByte(s);
        }
    };

    static final StringConverter TO_FLOAT = new StringConverter<Float>(FieldReflection.Type.FLOAT) {
        @Override
        Float convert(String s) {
            return toFloat(s);
        }
    };

    static final StringConverter TO_DOUBLE = new StringConverter<Double>(FieldReflection.Type.DOUBLE) {
        @Override
        Double convert(String s) {
            return toDouble(s);
        }
    };

    static final StringConverter TO_DATE = new StringConverter<Date>(FieldReflection.Type.OBJECT) {
        @Override
        Date convert(String s) {
            return toDate(s);
        }
    };

    static final StringConverter TO_STRING = new StringConverter<String>(FieldReflection.Type.OBJECT) {
        @Override
        String convert(String s) {
            return s;
        }
    };

    protected StringConverter(FieldReflection.Type type) {
        this.type = type;
    }

    abstract T convert(String s);

    static StringConverter getConverter(Class clazz) {
        if (clazz == String.class)
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

        return null;
    }

    static Integer toInteger(String s) {
        return Integer.valueOf(s);
    }

    static Long toLong(String s) {
        return Long.valueOf(s);
    }

    static Byte toByte(String s) {
        return Byte.valueOf(s);
    }

    static Short toShort(String s) {
        return Short.valueOf(s);
    }

    static Boolean toBoolean(String s) {
        return Boolean.valueOf(s);
    }

    static Float toFloat(String s) {
        return Float.valueOf(s);
    }

    static Double toDouble(String s) {
        return Double.valueOf(s);
    }

    static Date toDate(String s) {
        return DateIso8601.parse(s);
    }
}
