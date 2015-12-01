package com.wizzardo.tools.reflection;

/**
 * @author: wizzardo
 * Date: 8/8/14
 */
public class StringReflection {

    private static StringReflections reflections;

    static {
        FieldReflection value = getFieldReflection(String.class, "value", true);
        FieldReflection offset = getFieldReflection(String.class, "offset", false);
        FieldReflection count = getFieldReflection(String.class, "count", false);
        FieldReflection hash = getFieldReflection(String.class, "hash", false);
        FieldReflection coder = getFieldReflection(String.class, "coder", false);
        if (hash == null)
            hash = getFieldReflection(String.class, "hashCode", true);

        if (value == null || hash == null || coder != null)
            reflections = new NoStringReflections();
        else if (offset != null && count != null)
            reflections = new StringReflectionsJava6(value, hash, count, offset);
        else
            reflections = new StringReflectionsJava7(value, hash);
    }

    interface StringReflections {
        int offset(String s);

        char[] chars(String s);

        String createString(char[] chars);

        String createString(char[] chars, int hash);
    }

    static class StringReflectionsJava6 implements StringReflections {
        final FieldReflection value;
        final FieldReflection hash;
        final FieldReflection count;
        final FieldReflection offset;

        StringReflectionsJava6(FieldReflection value, FieldReflection hash, FieldReflection count, FieldReflection offset) {
            this.value = value;
            this.hash = hash;
            this.count = count;
            this.offset = offset;
        }

        @Override
        public int offset(String s) {
            return offset.getInteger(s);
        }

        @Override
        public char[] chars(String s) {
            return (char[]) value.getObject(s);
        }

        @Override
        public String createString(char[] chars) {
            String s = new String();
            value.setObject(s, chars);
            count.setInteger(s, chars.length);
            return s;
        }

        @Override
        public String createString(char[] chars, int hash) {
            String s = new String();
            value.setObject(s, chars);
            count.setInteger(s, chars.length);

            if (hash != 0)
                this.hash.setInteger(s, hash);
            return s;
        }
    }

    static class StringReflectionsJava7 implements StringReflections {
        final FieldReflection value;
        final FieldReflection hash;

        StringReflectionsJava7(FieldReflection value, FieldReflection hash) {
            this.value = value;
            this.hash = hash;
        }

        @Override
        public int offset(String s) {
            return 0;
        }

        @Override
        public char[] chars(String s) {
            return (char[]) value.getObject(s);
        }

        @Override
        public String createString(char[] chars) {
            String s = new String();
            value.setObject(s, chars);
            return s;
        }

        @Override
        public String createString(char[] chars, int hash) {
            String s = new String();
            value.setObject(s, chars);
            if (hash != 0)
                this.hash.setInteger(s, hash);
            return s;
        }
    }

    static class NoStringReflections implements StringReflections {
        @Override
        public int offset(String s) {
            return 0;
        }

        @Override
        public char[] chars(String s) {
            return s.toCharArray();
        }

        @Override
        public String createString(char[] chars) {
            return new String(chars);
        }

        @Override
        public String createString(char[] chars, int hash) {
            return new String(chars);
        }
    }

    private static FieldReflection getFieldReflection(Class clazz, String fieldName, boolean printStackTrace) {
        try {
            return new FieldReflectionFactory().create(clazz, fieldName, true);
        } catch (NoSuchFieldException e) {
            if (printStackTrace)
                e.printStackTrace();
        }
        return null;
    }

    public static char[] chars(String s) {
        return reflections.chars(s);
    }

    public static int offset(String s) {
        return reflections.offset(s);
    }

    public static String createString(char[] chars) {
        return reflections.createString(chars);
    }

    public static String createString(char[] chars, int hash) {
        return reflections.createString(chars, hash);
    }
}