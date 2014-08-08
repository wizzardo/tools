package com.wizzardo.tools.reflection;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 8/8/14
 */
public class StringReflection {
    private static FieldReflection value;
    private static FieldReflection hash;
    private static FieldReflection count;
    private static FieldReflection offset;

    static {
        try {
            Field value = String.class.getDeclaredField("value");
            value.setAccessible(true);
            StringReflection.value = new FieldReflection(value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            Field hash = String.class.getDeclaredField("hash");
            hash.setAccessible(true);
            StringReflection.hash = new FieldReflection(hash);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            Field offset = String.class.getDeclaredField("offset");
            offset.setAccessible(true);
            StringReflection.offset = new FieldReflection(offset);
        } catch (NoSuchFieldException ignored) {
        }
        try {
            Field count = String.class.getDeclaredField("count");
            count.setAccessible(true);
            StringReflection.count = new FieldReflection(count);
        } catch (NoSuchFieldException ignored) {
        }
    }

    public static char[] chars(String s) {
        return (char[]) value.getObject(s);
    }

    public static int offset(String s) {
        if (offset != null)
            return offset.getInteger(s);
        return 0;
    }

    public static String createString(char[] chars) {
        String s = new String();

        value.setObject(s, chars);
        if (count != null)
            count.setInteger(s, chars.length);
        return s;
    }

    public static String createString(char[] chars, int hash) {
        String s = new String();
        value.setObject(s, chars);

        if (hash != 0)
            StringReflection.hash.setInteger(s, hash);

        if (count != null)
            count.setInteger(s, chars.length);
        return s;
    }
}