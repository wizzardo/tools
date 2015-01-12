package com.wizzardo.tools.reflection;

import com.wizzardo.tools.avian.AvianTools;

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
        init();
    }

    private static void init(){
        if (AvianTools.IS_AVIAN_VM)
            return;

        value = getFieldReflection(String.class, "value", true);
        offset = getFieldReflection(String.class, "offset", false);
        count = getFieldReflection(String.class, "count", false);
        hash = getFieldReflection(String.class, "hash", false);
        if (hash == null)
            hash = getFieldReflection(String.class, "hashCode", true);
    }

    private static FieldReflection getFieldReflection(Class clazz, String fieldName, boolean printStackTrace) {
        try {
            return new FieldReflection(clazz, fieldName, true);
        } catch (NoSuchFieldException e) {
            if (printStackTrace)
                e.printStackTrace();
        }
        return null;
    }

    public static char[] chars(String s) {
        if (value == null)
            return s.toCharArray();

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