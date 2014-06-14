package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldSetter;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/21/14
 */
class JsonUtils {

    static int trimRight(char[] s, int from, int to) {
        while ((from < to) && (s[to - 1] <= ' ')) {
            to--;
        }
        return to;
    }

    static boolean isNull(char[] s, int from, int length) {
        return length == 4 && s[from] == 'n' && s[from + 1] == 'u' && s[from + 2] == 'l' && s[from + 3] == 'l';
    }

    static boolean isTrue(char[] s, int from, int length) {
        return length == 4 && s[from] == 't' && s[from + 1] == 'r' && s[from + 2] == 'u' && s[from + 3] == 'e';
    }

    static boolean isFalse(char[] s, int from, int length) {
        return length == 5 && s[from] == 'f' && s[from + 1] == 'a' && s[from + 2] == 'l' && s[from + 3] == 's' && s[from + 4] == 'e';
    }

    static int skipSpaces(char[] s, int from, int to) {
        while (from < to && s[from] <= ' ') {
            from++;
        }
        return from;
    }

    static int parseNumber(JsonBinder binder, char[] s, int from, int to) {
        int i = from;

        boolean minus = s[from] == '-';
        if (minus)
            i++;

        if (i == to)
            throw new IllegalStateException("no number found, only '-'");

        boolean floatValue = false;
        long l = 0;
        char ch;
        for (; i < to; i++) {
            ch = s[i];
            if (ch >= '0' && ch <= '9') {
                if (!floatValue)
                    l = l * 10 + (ch - '0');
            } else if (ch == '.' && !floatValue)
                floatValue = true;
            else
                break;

        }
        if (minus)
            l = -l;

        double d = 0;
        if (floatValue) {
            if (minus)
                from++;
            d = Double.parseDouble(new String(s, from, i - from));
            if (minus)
                d = -d;
        }

        JsonFieldSetter setter = binder.getFieldSetter();
        if (setter != null && setter.getType() != FieldSetter.Type.OBJECT) {
            setNumber(setter, binder.getObject(), l, d, floatValue);
            return i;
        }

        if (!floatValue)
            set(setter, binder, l);
        else
            set(setter, binder, d);

        return i;
    }

    private static void setNumber(FieldSetter setter, Object object, long l, double d, boolean floatValue) {
        switch (setter.getType()) {
            case INTEGER: {
                setter.setInteger(object, (int) l);
                break;
            }
            case LONG: {
                setter.setLong(object, l);
                break;
            }
            case BYTE: {
                setter.setByte(object, (byte) l);
                break;
            }
            case SHORT: {
                setter.setShort(object, (short) l);
                break;
            }
            case CHAR: {
                setter.setChar(object, (char) l);
                break;
            }
            case FLOAT: {
                if (floatValue)
                    setter.setFloat(object, (float) d);
                else
                    setter.setFloat(object, (float) l);
                break;
            }
            case DOUBLE: {
                if (floatValue)
                    setter.setDouble(object, d);
                else
                    setter.setDouble(object, (double) l);
                break;
            }
            case BOOLEAN: {
                setter.setBooalen(object, l != 0);
                break;
            }
        }
    }

    static int parseValue(JsonBinder binder, char[] s, int from, int to, char end) {
        char ch = s[from];

        char quote = 0;
        if (ch == '"' || ch == '\'') {
            quote = ch;
            from++;
        }

        int i = from;
        boolean escape = false;
        boolean needDecoding = false;
        if (quote == 0) {
            for (; i < to; i++) {
                ch = s[i];
                if (ch <= ' ' || ch == ',' || ch == end)
                    break;

                if (ch == '\\')
                    needDecoding = true;
            }
        } else {
            for (; i < to; i++) {
                if (escape) {
                    escape = false;
                } else {
                    ch = s[i];
                    if (ch == quote)
                        break;

                    if (ch == '\\')
                        escape = needDecoding = true;
                }
            }
        }

        int k = i;
        if (quote == 0)
            k = trimRight(s, from, k);
        else
            i++;

        JsonFieldSetter setter = binder.getFieldSetter();
        String value;
        int l = k - from;
        if (!needDecoding) {
            boolean isTrue = isTrue(s, from, l);
            if (setter != null && setter.getType() == FieldSetter.Type.BOOLEAN) {
                setter.setBooalen(binder.getObject(), isTrue);
                return i;
            }

            if (JsonUtils.isNull(s, from, l)) {
                set(setter, binder, null);
                return i;
            }
            if (isTrue) {
                set(setter, binder, Boolean.TRUE);
                return i;
            }
            if (isFalse(s, from, l)) {
                set(setter, binder, Boolean.FALSE);
                return i;
            }

            value = new String(s, from, l);
        } else
            value = JsonObject.unescape(s, from, k);

        set(setter, binder, value);

        return i;
    }

    private static void set(JsonFieldSetter setter, JsonBinder binder, Object value) {
        if (setter != null)
            setter.set(binder.getObject(), new JsonItem(value));
        else
            binder.add(value);
    }

    static int parseKey(JsonBinder binder, char[] s, int from, int to) {
        int i = from;
        char ch = s[i];

        char quote = 0;
        if (ch == '\'' || ch == '"') {
            quote = ch;
            i++;
            from++;
        }

        int rigthBorder;
        boolean escape = false;
        boolean needDecoding = false;
        if (quote == 0) {
            for (; i < to; i++) {
                ch = s[i];
                if (ch <= ' ' || ch == ':' || ch == '}')
                    break;

                if (ch == '\\')
                    needDecoding = true;
            }
            rigthBorder = trimRight(s, from, i);
        } else {
            for (; i < to; i++) {
                if (escape) {
                    escape = false;
                } else {
                    ch = s[i];
                    if (ch == quote)
                        break;

                    if (ch == '\\')
                        escape = needDecoding = true;
                }
            }
            rigthBorder = i;
            i++;
        }

        i = skipSpaces(s, i, to);

        if (s[i] != ':')
            throw new IllegalStateException("here must be key-value separator ':', but found: " + s[i]);


        if (rigthBorder == from)
            throw new IllegalStateException("key not found");

        String value;
        if (!needDecoding)
            value = new String(s, from, rigthBorder - from);
        else
            value = JsonObject.unescape(s, from, rigthBorder);

        binder.setTemporaryKey(value);

        return i + 1;
    }

    static char[] toCharArray(String s) {
        return CharArrayExtractor.extract(s);
    }

    static int getCharArrayOffset(String s) {
        return CharArrayExtractor.getOffset(s);
    }

    private static class CharArrayExtractor {
        static Field array;
        static Field offset;

        static {
            try {
                array = String.class.getDeclaredField("value");
                array.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            try {
                offset = String.class.getDeclaredField("offset");
                offset.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }
        }

        static char[] extract(String s) {
            try {
                return (char[]) array.get(s);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return s.toCharArray();
        }

        static int getOffset(String s) {
            try {
                return (Integer) offset.get(s);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }
}
