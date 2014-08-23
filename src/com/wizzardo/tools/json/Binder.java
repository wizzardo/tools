package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.DateIso8601;
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
    private static Map<Class, Serializer> serializers = new ConcurrentHashMap<Class, Serializer>();

    static abstract class Serializer {
        static char[] nullArray = new char[]{'n', 'u', 'l', 'l'};
        final SerializerType type;

        protected Serializer(SerializerType type) {
            this.type = type;
        }

        public void checkNullAndSerialize(Object object, Appender appender, Generic generic) {
            if (object == null)
                appender.append(nullArray);
            else
                serialize(object, appender, generic);

        }

        abstract public void serialize(Object object, Appender appender, Generic generic);
    }

    private static class ArrayBoxedSerializer extends Serializer {
        Serializer serializer;

        protected ArrayBoxedSerializer(Serializer serializer) {
            super(SerializerType.ARRAY);
            this.serializer = serializer;
        }

        @Override
        public void serialize(Object src, Appender sb, Generic generic) {
            Object[] arr = (Object[]) src;
            int length = arr.length;

            sb.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(',');
                serializer.checkNullAndSerialize(arr[i], sb, null);
            }
            sb.append(']');
        }
    }

    private static Serializer stringSerializer = new Serializer(SerializerType.STRING) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appendString(object, appender);
        }
    };
    private static Serializer characterSerializer = new Serializer(SerializerType.STRING) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appender.append('"');
            JsonTools.escape((Character) object, appender);
            appender.append('"');
        }
    };
    private static Serializer numberBooleanSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appendNumberOrBoolean(object, appender);
        }
    };
    private static Serializer collectionSerializer = new Serializer(SerializerType.COLLECTION) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appendCollection(object, appender, generic);
        }
    };
    private static Serializer arraySerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appendArray(object, appender, generic);
        }
    };
    private static Serializer arrayIntSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            int[] arr = (int[]) object;
            int length = arr.length;

            appender.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) appender.append(',');
                appender.append(String.valueOf(arr[i]));
            }
            appender.append(']');
        }
    };
    private static Serializer arrayLongSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            long[] arr = (long[]) object;
            int length = arr.length;

            appender.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) appender.append(',');
                appender.append(String.valueOf(arr[i]));
            }
            appender.append(']');
        }
    };
    private static Serializer arrayByteSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            byte[] arr = (byte[]) object;
            int length = arr.length;

            appender.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) appender.append(',');
                appender.append(String.valueOf(arr[i]));
            }
            appender.append(']');
        }
    };
    private static Serializer arrayShortSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            short[] arr = (short[]) object;
            int length = arr.length;

            appender.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) appender.append(',');
                appender.append(String.valueOf(arr[i]));
            }
            appender.append(']');
        }
    };
    private static Serializer arrayBooleanSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            boolean[] arr = (boolean[]) object;
            int length = arr.length;

            appender.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) appender.append(',');
                appender.append(String.valueOf(arr[i]));
            }
            appender.append(']');
        }
    };
    private static Serializer arrayFloatSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            float[] arr = (float[]) object;
            int length = arr.length;

            appender.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) appender.append(',');
                appender.append(String.valueOf(arr[i]));
            }
            appender.append(']');
        }
    };
    private static Serializer arrayDoubleSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            double[] arr = (double[]) object;
            int length = arr.length;

            appender.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) appender.append(',');
                appender.append(String.valueOf(arr[i]));
            }
            appender.append(']');
        }
    };
    private static Serializer arrayCharSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            char[] chars = (char[]) object;
            appender.append('[');
            int to = chars.length;
            int from = 0;
            for (int i = from; i < to; i++) {
                if (i > 0)
                    appender.append(',');
                appender.append('"');
                JsonTools.escape(chars[i], appender);
                appender.append('"');
            }
            appender.append(']');
        }
    };
    private static Serializer mapSerializer = new Serializer(SerializerType.MAP) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appendMap(object, appender, generic);
        }
    };
    private static Serializer dateSerializer = new Serializer(SerializerType.DATE) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appender.append('"');
            appender.append(DateIso8601.formatToChars((Date) object));
            appender.append('"');
        }
    };
    private static Serializer enumSerializer = new Serializer(SerializerType.ENUM) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appender.append('"');
            appender.append(object);
            appender.append('"');
        }
    };
    private static Serializer nullSerializer = new Serializer(SerializerType.NULL) {

        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appender.append(nullArray);
        }
    };
    private static Serializer objectSerializer = new Serializer(SerializerType.OBJECT) {
        @Override
        public void serialize(Object src, Appender sb, Generic generic) {
            sb.append('{');
            boolean comma = false;
            Map<String, FieldInfo> list = getFields(src.getClass());

            for (FieldInfo info : list.values()) {
                Field field = info.field;
                try {
                    if (comma)
                        sb.append(',');
                    else
                        comma = true;

                    appendName(field.getName(), sb, false);
                    info.serializer.checkNullAndSerialize(field.get(src), sb, info.generic);
                } catch (IllegalAccessException e) {
                    throw new WrappedException(e);
                }
            }
            sb.append('}');
        }
    };
    private static Serializer numberBooleanBoxedSerializer = new ArrayBoxedSerializer(numberBooleanSerializer);
    private static Serializer stringArraySerializer = new ArrayBoxedSerializer(stringSerializer);
    private static Serializer charArraySerializer = new ArrayBoxedSerializer(characterSerializer);
    private static Serializer dateArraySerializer = new ArrayBoxedSerializer(dateSerializer);
    private static Serializer enumArraySerializer = new ArrayBoxedSerializer(enumSerializer);
    private static Serializer collectionArraySerializer = new ArrayBoxedSerializer(collectionSerializer);
    private static Serializer mapArraySerializer = new ArrayBoxedSerializer(mapSerializer);
    private static Serializer arrayArraySerializer = new ArrayBoxedSerializer(arraySerializer);


    static enum SerializerType {
        STRING,
        NUMBER_BOOLEAN,
        COLLECTION,
        ARRAY,
        MAP,
        DATE,
        OBJECT,
        ENUM,
        NULL
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
        SerializerType serializer = classToSerializer(clazz).type;

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
            fields = new LinkedHashMap<String, FieldInfo>();
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

    static Serializer getReturnType(Field field) {
        return classToSerializer(field.getType());
    }

    static Serializer classToSerializer(Class clazz) {
        Serializer serializer = serializers.get(clazz);
        if (serializer != null)
            return serializer;

        serializer = classToSerializerWithoutCache(clazz);
        serializers.put(clazz, serializer);
        return serializer;
    }

    static Serializer classToSerializerWithoutCache(Class clazz) {
        if (String.class == clazz)
            return stringSerializer;
        else if (clazz.isPrimitive() || Boolean.class == clazz || Number.class.isAssignableFrom(clazz))
            return numberBooleanSerializer;
        else if (Collection.class.isAssignableFrom(clazz))
            return collectionSerializer;
        else if (Map.class.isAssignableFrom(clazz))
            return mapSerializer;
        else if (Date.class.isAssignableFrom(clazz))
            return dateSerializer;
        else if (Array.class == clazz || clazz.isArray()) {
            clazz = clazz.getComponentType();
            if (clazz != null && clazz.isPrimitive()) {
                if (clazz == int.class)
                    return arrayIntSerializer;
                if (clazz == long.class)
                    return arrayLongSerializer;
                if (clazz == byte.class)
                    return arrayByteSerializer;
                if (clazz == short.class)
                    return arrayShortSerializer;
                if (clazz == char.class)
                    return arrayCharSerializer;
                if (clazz == float.class)
                    return arrayFloatSerializer;
                if (clazz == double.class)
                    return arrayDoubleSerializer;
                if (clazz == boolean.class)
                    return arrayBooleanSerializer;
                if (clazz == Float.class ||
                        clazz == Double.class ||
                        clazz == Byte.class ||
                        clazz == Short.class ||
                        clazz == Integer.class ||
                        clazz == Long.class ||
                        clazz == Boolean.class)
                    return numberBooleanBoxedSerializer;
                if (clazz == Character.class)
                    return charArraySerializer;
                if (clazz == String.class)
                    return stringArraySerializer;
                if (Date.class.isAssignableFrom(clazz))
                    return dateArraySerializer;
                if (Collection.class.isAssignableFrom(clazz))
                    return collectionArraySerializer;
                if (Map.class.isAssignableFrom(clazz))
                    return mapArraySerializer;
                if (Array.class == clazz || clazz.isArray())
                    return arrayArraySerializer;
                if (clazz.isEnum())
                    return enumArraySerializer;
            }
            return arraySerializer;
        } else if (clazz.isEnum())
            return enumSerializer;
        else
            return objectSerializer;
    }

    public static <T> T fromJSON(Class<T> clazz, JsonItem json) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return fromJSON(clazz, json, null);
    }

    public static <T> T fromJSON(Class<T> clazz, JsonItem json, Type generic) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (clazz == null || json == null) {
            return null;
        }

        Serializer serializer = classToSerializer(clazz);

        switch (serializer.type) {
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
                    SerializerType s = info.serializer.type;
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
        public abstract void append(String s);

        public abstract void append(String s, int from, int to);

        public abstract void append(char[] s, int from, int to);

        public abstract void append(char s);

        public void append(Object ob) {
            append(String.valueOf(ob));
        }

        public void append(char[] chars) {
            append(chars, 0, chars.length);
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
        public void append(String s) {
            sb.append(s);
        }

        @Override
        public void append(String s, int from, int to) {
            sb.append(s, from, to);
        }

        @Override
        public void append(char[] s, int from, int to) {
            sb.append(s, from, to - from);
        }

        @Override
        public void append(char s) {
            sb.append(s);
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    static class StreamAppender extends Appender {
        private OutputStreamWriter out;

        StreamAppender(OutputStream out) {
            this.out = new OutputStreamWriter(out);
        }

        @Override
        public void append(String s) {
            try {
                out.write(s);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
        }

        @Override
        public void append(String s, int from, int to) {
            try {
                out.append(s, from, to);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
        }

        @Override
        public void append(char[] s, int from, int to) {
            try {
                out.write(s, from, to - from);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
        }

        @Override
        public void append(char s) {
            try {
                out.append(s);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
        }
    }

    static void toJSON(Object src, Appender sb) {
        if (src == null) {
            nullSerializer.serialize(src, sb, null);
            return;
        }

        classToSerializer(src.getClass()).serialize(src, sb, null);
    }

    private static void toJSON(String name, Object src, Appender sb) {
        Serializer serializer;
        if (src != null)
            serializer = classToSerializer(src.getClass());
        else
            serializer = nullSerializer;
        appendName(name, sb, true);
        serializer.serialize(src, sb, null);
    }

    private static void appendNumberOrBoolean(Object ob, Appender sb) {
        sb.append(ob);
    }

    private static void appendString(Object ob, Appender sb) {
        sb.append('"');
        JsonTools.escape((String) ob, sb);
        sb.append('"');
    }

    private static void appendCollection(Object src, Appender sb, Generic generic) {
        Iterator i = ((Collection) src).iterator();

        Serializer serializer = null;
        Generic inner = null;
        if (generic != null && generic.typeParameters.length == 1) {
            serializer = generic.typeParameters[0].serializer;
            inner = generic.typeParameters[0];
        }

        sb.append('[');
        boolean next;
        if (i.hasNext())
            if (serializer != null)
                do {
                    serializer.checkNullAndSerialize(i.next(), sb, inner);
                    if (next = i.hasNext())
                        sb.append(',');
                } while (next);
            else
                do {
                    toJSON(i.next(), sb);
                    if (next = i.hasNext())
                        sb.append(',');
                } while (next);
        sb.append(']');
    }

    private static void appendArray(Object src, Appender sb, Generic generic) {
        Object[] arr = (Object[]) src;
        int length = arr.length;

        Serializer serializer;
        Generic inner = null;
        if (generic != null && generic.typeParameters.length == 1) {
            serializer = generic.typeParameters[0].serializer;
            inner = generic.typeParameters[0];
        } else
            serializer = objectSerializer;

        sb.append('[');
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(',');
            serializer.checkNullAndSerialize(arr[i], sb, inner);
        }
        sb.append(']');
    }

    private static void appendMap(Object src, Appender sb, Generic generic) {
        sb.append('{');
        Iterator<Map.Entry> i = ((Map) src).entrySet().iterator();

        Serializer serializer = null;
        Generic inner = null;
        if (generic != null && generic.typeParameters.length == 2) {
            serializer = generic.typeParameters[1].serializer;
            inner = generic.typeParameters[1];
        }
        boolean next;
        if (i.hasNext())
            if (serializer != null)
                do {
                    Map.Entry entry = i.next();
                    appendName(String.valueOf(entry.getKey()), sb, true);
                    serializer.checkNullAndSerialize(entry.getValue(), sb, inner);
                    if (next = i.hasNext())
                        sb.append(',');
                } while (next);
            else
                do {
                    Map.Entry entry = i.next();
                    toJSON(String.valueOf(entry.getKey()), entry.getValue(), sb);
                    if (next = i.hasNext())
                        sb.append(',');
                } while (next);
        sb.append('}');
    }

    private static void appendName(String name, Appender sb, boolean escape) {
        if (name != null) {
            sb.append('"');
            if (escape)
                JsonTools.escape(name, sb);
            else
                sb.append(name);
            sb.append('"');
            sb.append(':');
        }
    }
}
