package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.DateIso8601;
import com.wizzardo.tools.misc.SoftThreadLocal;
import com.wizzardo.tools.misc.WrappedException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: moxa
 * Date: 3/23/13
 */
public class Binder {

    private static final int SYNTHETIC = 0x00001000;
    private static Map<Class, Map<String, FieldInfo>> cachedFields = new ConcurrentHashMap<Class, Map<String, FieldInfo>>();
    private static Map<Class, Constructor> cachedConstructors = new ConcurrentHashMap<Class, Constructor>();
    private static SoftThreadLocal<StringBuilder> stringBuilderThreadLocal = new SoftThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder init() {
            return new StringBuilder();
        }

        @Override
        public StringBuilder getValue() {
            StringBuilder sb = super.getValue();
            sb.setLength(0);
            return sb;
        }
    };

    enum Serializer {
        STRING, NUMBER_BOOLEAN, COLLECTION, ARRAY, MAP, DATE, OBJECT, ENUM
    }

    static JsonBinder getObjectBinder(Generic generic) {
        if (generic == null || generic.clazz == null)
            return new JsonObjectBinder();
        else if (Map.class.isAssignableFrom(generic.clazz))
            return new JavaMapBinder(generic);
        else
            return new JavaObjectBinder(generic);
    }

    static JsonBinder getArrayBinder(Generic generic) {
        if (generic == null || generic.clazz == null)
            return new JsonArrayBinder();
        else
            return new JavaArrayBinder(generic);
    }

    static <T> T createInstance(Class<T> clazz) {
        Serializer serializer = classToSerializer(clazz);

        switch (serializer) {
            case STRING:
            case NUMBER_BOOLEAN: {
                throw new IllegalArgumentException("can't create an instance of " + clazz);
            }
            case OBJECT: {
                Constructor c = cachedConstructors.get(clazz);
                if (c == null) {
                    try {
                        c = clazz.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new WrappedException(e);
                    }
                    c.setAccessible(true);
                    cachedConstructors.put(clazz, c);
                }
                try {
                    return (T) c.newInstance();
                } catch (InstantiationException e) {
                    throw new WrappedException(e);
                } catch (IllegalAccessException e) {
                    throw new WrappedException(e);
                } catch (InvocationTargetException e) {
                    throw new WrappedException(e);
                }
            }
            case ARRAY: {
                Object array = createArray(clazz, 0);
                return (T) array;
            }
            case COLLECTION: {
                Collection collection = createCollection(clazz);
                return (T) collection;
            }
            case MAP: {
                Map map = createMap(clazz);
                return (T) map;
            }
        }
        return null;
    }

    public static Map<String, FieldInfo> getFields(Class clazz) {
        Map<String, FieldInfo> fields = cachedFields.get(clazz);
        if (fields == null) {
            fields = new HashMap<String, FieldInfo>();
            Class cl = clazz;
            while (cl != null) {
                Field[] ff = cl.getDeclaredFields();
                for (Field field : ff) {
//                    System.out.println("field " + field);
                    if (
                            !Modifier.isTransient(field.getModifiers())
                                    && !Modifier.isStatic(field.getModifiers())
                                    && (field.getModifiers() & SYNTHETIC) == 0
//                                    && !Modifier.isFinal(field.getModifiers())
//                                    && !Modifier.isPrivate(field.getModifiers())
//                                    && !Modifier.isProtected(field.getModifiers())
                            ) {
//                        System.out.println("add field " + field);
                        field.setAccessible(true);
                        fields.put(field.getName(), new FieldInfo(field, getReturnType(field)));
                    }
                }
                cl = cl.getSuperclass();
            }
            cachedFields.put(clazz, fields);
        }
        return fields;
    }

    public static FieldInfo getField(Class clazz, String key) {
        return getFields(clazz).get(key);
    }

    public static boolean setValue(Object object, String key, JsonItem value) {
        Class clazz = object.getClass();
        return setValue(object, getField(clazz, key), value);
    }

    public static boolean setValue(Object object, FieldInfo fieldInfo, JsonItem value) {
        if (fieldInfo == null)
            return false;

        fieldInfo.setter.set(object, value);
//        Field field = fieldInfo.field;
//
//        try {
//            switch (fieldInfo.serializer) {
//                case STRING:
//                case NUMBER_BOOLEAN:
//                    fieldInfo.setter.set(object, value);
////                    field.set(object, JsonItem.getAs(value, field.getType()));
////                    JsonItem.setField(value, field,object);
//                    break;
//                case ENUM:
//                    if (value == null)
//                        field.set(object, value);
//                    else {
////                        Class c = field.getType();
////                        field.set(object, Enum.valueOf(c, value.asString()));
//                        fieldInfo.setter.set(object, value);
//                    }
//                    break;
//                default:
////                    field.set(object, value);
//                    fieldInfo.setter.set(object, value);
//            }
//        } catch (IllegalAccessException e) {
//            throw new WrappedException(e);
//        }
        return true;
    }


    static Serializer getReturnType(Method method) {
        return classToSerializer(method.getReturnType());
    }

    static Serializer getReturnType(Field field) {
        return classToSerializer(field.getType());
    }

    static Serializer classToSerializer(Class clazz) {
        if (String.class == clazz)
            return Serializer.STRING;
        else if (clazz.isPrimitive() || Boolean.class == clazz || Number.class.isAssignableFrom(clazz))
            return Serializer.NUMBER_BOOLEAN;
        else if (Collection.class.isAssignableFrom(clazz))
            return Serializer.COLLECTION;
        else if (Map.class.isAssignableFrom(clazz))
            return Serializer.MAP;
        else if (Date.class.isAssignableFrom(clazz))
            return Serializer.DATE;
        else if (Array.class == clazz || clazz.getName().charAt(0) == '[')
            return Serializer.ARRAY;
        else if (clazz.isEnum())
            return Serializer.ENUM;
        else
            return Serializer.OBJECT;
    }

    public static <T> T fromJSON(Class<T> clazz, JsonItem json) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return fromJSON(clazz, json, null);
    }

    public static <T> T fromJSON(Class<T> clazz, JsonItem json, Type generic) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (clazz == null || json == null) {
            return null;
        }

        Serializer serializer = classToSerializer(clazz);

        switch (serializer) {
            case STRING:
            case NUMBER_BOOLEAN: {
                return json.getAs(clazz);
            }
            case OBJECT: {
                T instance = null;
                Constructor c = cachedConstructors.get(clazz);
                if (c == null) {
                    c = clazz.getDeclaredConstructor();
                    c.setAccessible(true);
                    cachedConstructors.put(clazz, c);
                }
                instance = (T) c.newInstance();

                for (FieldInfo info : getFields(clazz).values()) {
                    Field field = info.field;
                    Serializer s = info.serializer;
                    JsonObject jsonObject = json.asJsonObject();
                    JsonItem item = jsonObject.get(field.getName());
                    if (item != null)
                        switch (s) {
                            case STRING:
                            case NUMBER_BOOLEAN:
                            case ARRAY:
                                field.set(instance, fromJSON(field.getType(), item));
                                break;
                            case COLLECTION:
                                field.set(instance, fromJSON(field.getType(), item, field.getGenericType()));
                                break;
                            default:
                                field.set(instance, fromJSON(field.getType(), item));
                        }

                }

                return instance;
            }
            case ARRAY: {
                Object array = createArray(clazz, json.asJsonArray().size());
                Class type = getArrayType(clazz);
                JsonArray jsonArray = json.asJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    Array.set(array, i, fromJSON(type, jsonArray.get(i)));
                }
                return (T) array;
            }
            case COLLECTION: {
                Collection collection = createCollection(clazz);
                Type type = null;
                if (generic != null && generic instanceof ParameterizedType)
                    type = ((ParameterizedType) generic).getActualTypeArguments()[0];

                Class cl = null;
                Type inner = null;
                if (type != null)
                    if (type instanceof ParameterizedType) {
                        cl = (Class) ((ParameterizedType) type).getRawType();
                        inner = type;
                    } else
                        cl = (Class) type;

                if (cl == null)
                    cl = String.class;

                for (JsonItem item : json.asJsonArray()) {
                    collection.add(fromJSON(cl, item, inner));
                }
                return (T) collection;
            }
        }

        return null;
    }

    static Collection createCollection(Class clazz) {
        Constructor c = cachedConstructors.get(clazz);
        try {
            if (c == null) {
                if (clazz == List.class) {
                    c = ArrayList.class.getDeclaredConstructor();
                } else if (clazz == Set.class) {
                    c = HashSet.class.getDeclaredConstructor();
                } else {
                    c = clazz.getDeclaredConstructor();
                }
                cachedConstructors.put(clazz, c);
            }
            return (Collection) c.newInstance();
        } catch (IllegalAccessException e) {
            throw new WrappedException(e);
        } catch (NoSuchMethodException e) {
            throw new WrappedException(e);
        } catch (InstantiationException e) {
            throw new WrappedException(e);
        } catch (InvocationTargetException e) {
            throw new WrappedException(e);
        }
    }

    static Map createMap(Class clazz) {
        Constructor c = cachedConstructors.get(clazz);
        try {
            if (c == null) {
                if (clazz == Map.class) {
                    c = HashMap.class.getDeclaredConstructor();
                } else {
                    c = clazz.getDeclaredConstructor();
                }
                cachedConstructors.put(clazz, c);
            }
            return (Map) c.newInstance();
        } catch (IllegalAccessException e) {
            throw new WrappedException(e);
        } catch (NoSuchMethodException e) {
            throw new WrappedException(e);
        } catch (InstantiationException e) {
            throw new WrappedException(e);
        } catch (InvocationTargetException e) {
            throw new WrappedException(e);
        }
    }

    static Object createArray(Class clazz, int size) {
        return createArrayByComponentType(clazz.getComponentType(), size);
    }

    static Object createArrayByComponentType(Class clazz, int size) {
        return Array.newInstance(clazz, size);
    }

    static Object createArray(Generic generic, int size) {
        return createArrayByComponentType(generic.typeParameters[0].clazz, size);
    }

    static Class getArrayType(Class clazz) {
        return clazz.getComponentType();
    }

    static abstract class Appender {
        public abstract Appender append(String s);

        public abstract Appender append(String s, int from, int to);

        public abstract Appender append(char[] s, int from, int to);

        public abstract Appender append(char s);

        public Appender append(Object ob) {
            return append(String.valueOf(ob));
        }

        public Appender append(char[] chars) {
            return append(chars, 0, chars.length);
        }
    }

    static class StringBuilderAppender extends Appender {
        private StringBuilder sb;

        StringBuilderAppender() {
            this(new StringBuilder());
        }

        StringBuilderAppender(StringBuilder sb) {
            this.sb = sb;
        }

        @Override
        public Appender append(String s) {
            sb.append(s);
            return this;
        }

        @Override
        public Appender append(String s, int from, int to) {
            sb.append(s, from, to);
            return this;
        }

        @Override
        public Appender append(char[] s, int from, int to) {
            sb.append(s, from, to - from);
            return this;
        }

        @Override
        public Appender append(char s) {
            sb.append(s);
            return this;
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    static class StreamAppender extends Appender {
        private OutputStreamWriter out;

        private StreamAppender(OutputStream out) {
            this.out = new OutputStreamWriter(out);
        }

        @Override
        public Appender append(String s) {
            try {
                out.write(s);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
            return this;
        }

        @Override
        public Appender append(String s, int from, int to) {
            try {
                out.append(s, from, to);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
            return this;
        }

        @Override
        public Appender append(char[] s, int from, int to) {
            try {
                out.write(s, from, to - from);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
            return this;
        }

        @Override
        public Appender append(char s) {
            try {
                out.append(s);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
            return this;
        }
    }

    public static String toJSON(Object src) {
        StringBuilderAppender sb = new StringBuilderAppender(stringBuilderThreadLocal.getValue());
        toJSON(src, sb);
        return sb.toString();
    }

    public static void toJSON(Object src, OutputStream out) {
        toJSON(src, new StreamAppender(out));
    }

    public static void toJSON(Object src, StringBuilder out) {
        toJSON(src, new StringBuilderAppender(out));
    }

    private static void toJSON(Object src, Appender sb) {
        if (src == null) {
            sb.append("null");
            return;
        }
        Serializer serializer;
        if ((serializer = classToSerializer(src.getClass())) != Serializer.OBJECT) {
            toJSON(null, src, sb, serializer, false);
            return;
        }

        sb.append('{');

        boolean comma = false;
        Map<String, FieldInfo> list = getFields(src.getClass());

        for (FieldInfo info : list.values()) {
            Field field = info.field;
            try {
                if (comma)
                    sb.append(',');
                toJSON(field.getName(), field.get(src), sb, info.serializer, false);
                comma = true;
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
        }

        sb.append('}');
    }

    private static void toJSON(String name, Object src, Appender sb) {
        Serializer serializer = null;
        if (src != null)
            serializer = classToSerializer(src.getClass());
        toJSON(name, src, sb, serializer, true);
    }

    private static void appendNumberOrBoolean(Object ob, Appender sb) {
        sb.append(ob);
    }

    private static void appendString(Object ob, Appender sb) {
        sb.append('"');
        JsonObject.escape(ob.toString(), sb);
        sb.append('"');
    }

    private static void appendCollection(Object src, Appender sb) {
        Iterator i = ((Collection) src).iterator();
        sb.append('[');
        if (i.hasNext())
            do {
                toJSON(i.next(), sb);
            } while (i.hasNext() && sb.append(',') != null);
        sb.append(']');
    }

    private static void appendArray(Object src, Appender sb) {
        int length = Array.getLength(src);
        sb.append('[');
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(',');
            toJSON(Array.get(src, i), sb);
        }
        sb.append(']');
    }

    private static void appendMap(Object src, Appender sb) {
        sb.append('{');
        Iterator i = ((Map) src).entrySet().iterator();
        if (i.hasNext())
            do {
                Map.Entry entry = (Map.Entry) i.next();
                toJSON(String.valueOf(entry.getKey()), entry.getValue(), sb);
            } while (i.hasNext() && sb.append(',') != null);
        sb.append('}');
    }

    private static void appendName(String name, Appender sb, boolean escape) {
        if (name != null) {
            sb.append('"');
            if (escape)
                JsonObject.escape(name, sb);
            else
                sb.append(name);
            sb.append('"').append(':');
        }
    }

    private static void toJSON(String name, Object src, Appender sb, Serializer serializer, boolean escapeName) {
        appendName(name, sb, escapeName);

        if (src == null) {
            sb.append("null");
            return;
        }

        switch (serializer) {
            case NUMBER_BOOLEAN: {
                appendNumberOrBoolean(src, sb);
                break;
            }
            case STRING: {
                appendString(src, sb);
                break;
            }
            case COLLECTION: {
                appendCollection(src, sb);
                break;
            }
            case MAP: {
                appendMap(src, sb);
                break;
            }
            case ARRAY: {
                appendArray(src, sb);
                break;
            }
            case DATE: {
                sb.append('"').append(DateIso8601.formatToChars((Date) src)).append('"');
                break;
            }
            case OBJECT: {
                toJSON(src, sb);
                break;
            }
            case ENUM: {
                sb.append('"').append(src).append('"');
                break;
            }
        }
    }
}
