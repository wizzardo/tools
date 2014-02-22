package com.wizzardo.tools.json;

import com.wizzardo.tools.Pair;
import com.wizzardo.tools.WrappedException;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: moxa
 * Date: 3/23/13
 */
public class Binder {

    private static final int SYNTHETIC = 0x00001000;
    private static Map<Class, Map<String, Pair<Pair<Field, GenericInfo>, Serializer>>> cachedFields = new ConcurrentHashMap<Class, Map<String, Pair<Pair<Field, GenericInfo>, Serializer>>>();
    private static Map<Class, Constructor> cachedConstructors = new ConcurrentHashMap<Class, Constructor>();

    enum Serializer {
        STRING, NUMBER_BOOLEAN, COLLECTION, ARRAY, MAP, DATE, OBJECT, ENUM
    }

    static ObjectBinder getObjectBinder(Class clazz) {
        if (clazz == null)
            return new JsonObjectBinder();
        else
            return new JavaObjectBinder(clazz);
    }

    static ArrayBinder getArrayBinder(Class clazz, GenericInfo genereic) {
        if (clazz == null)
            return new JsonArrayBinder();
        else
            return new JavaArrayBinder(clazz, genereic);
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
        }
        return null;
    }

    public static Map<String, Pair<Pair<Field, GenericInfo>, Serializer>> getFields(Class clazz) {
        Map<String, Pair<Pair<Field, GenericInfo>, Serializer>> fields = cachedFields.get(clazz);
        if (fields == null) {
            fields = new HashMap<String, Pair<Pair<Field, GenericInfo>, Serializer>>();
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
                        Pair<Field, GenericInfo> genericInfoPair = new Pair<Field, GenericInfo>(field, new GenericInfo(field.getGenericType()));
                        fields.put(field.getName(), new Pair<Pair<Field, GenericInfo>, Serializer>(genericInfoPair, getReturnType(field)));
                    }
                }
                cl = cl.getSuperclass();
            }
            cachedFields.put(clazz, fields);
        }
        return fields;
    }

    public static Pair<Pair<Field, GenericInfo>, Serializer> getField(Class clazz, String key) {
        return getFields(clazz).get(key);
    }

    public static void setValue(Object object, String key, Object value) {
        Class clazz = object.getClass();

        Pair<Pair<Field, GenericInfo>, Serializer> pair = getField(clazz, key);
        if (pair == null)
            return;

        Field field = pair.key.key;
        Serializer s = pair.value;

        try {
            switch (s) {
                case STRING:
                case NUMBER_BOOLEAN:
                    field.set(object, JsonItem.getAs(value, field.getType()));
                    break;
                case ENUM:
                    if (value == null)
                        field.set(object, value);
                    else {
                        Class c = field.getType();
                        field.set(object, Enum.valueOf(c, String.valueOf(value)));
                    }
                    break;
                default:
                    field.set(object, value);
            }
        } catch (IllegalAccessException e) {
            throw new WrappedException(e);
        }
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
        else if (clazz.getName().charAt(0) == '[')
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

                for (Pair<Pair<Field,GenericInfo>, Serializer> pair : getFields(clazz).values()) {
                    Field field = pair.key.key;
                    Serializer s = pair.value;
                    JsonObject jsonObject = json.asJsonObject();
                    JsonItem item = jsonObject.get(field.getName());
                    if (jsonObject.containsKey(field.getName()))
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

    static Object createArray(Class clazz, int size) {
        return Array.newInstance(clazz.getComponentType(), size);
    }

    static Class getArrayType(Class clazz) {
        return clazz.getComponentType();
    }

    private static boolean isGetter(Method method, Class clazz) {
        return (method.getName().startsWith("get") || method.getName().startsWith("is"))
                && !method.getName().equals("getClass")
                && (method.getParameterTypes() == null || method.getParameterTypes().length == 0)
                ;
    }

    static abstract class Appender {
        public abstract Appender append(String s);

        public Appender append(Object ob) {
            return append(String.valueOf(ob));
        }
    }

    static class StringBuilderAppender extends Appender {
        private StringBuilder sb = new StringBuilder();

        @Override
        public Appender append(String s) {
            sb.append(s);
            return this;
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    static class StreamAppender extends Appender {
        private OutputStream out;

        private StreamAppender(OutputStream out) {
            this.out = out;
        }

        @Override
        public Appender append(String s) {
            try {
                out.write(s.getBytes());
            } catch (IOException e) {
                throw new WrappedException(e);
            }
            return this;
        }
    }

    public static String toJSON(Object src) {
        StringBuilderAppender sb = new StringBuilderAppender();
        toJSON(src, sb);
        return sb.toString();
    }

    public static void toJSON(Object src, OutputStream out) {
        StreamAppender sb = new StreamAppender(out);
        toJSON(src, sb);
    }

    private static void toJSON(Object src, Appender sb) {
        if (src == null) {
            sb.append("null");
            return;
        }
        Serializer serializer;
        if ((serializer = classToSerializer(src.getClass())) != Serializer.OBJECT) {
            toJSON(null, src, sb, serializer);
            return;
        }
        sb.append("{");

        Set<String> fields = new HashSet<String>();
        boolean comma = false;
        Map<String, Pair<Pair<Field, GenericInfo>, Serializer>> list = getFields(src.getClass());

        for (Pair<Pair<Field, GenericInfo>, Serializer> pair : list.values()) {
            Field field = pair.key.key;
            try {
                if (comma)
                    sb.append(",");
                toJSON(field.getName(), field.get(src), sb, pair.value);
                fields.add(field.getName());
                comma = true;
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
        }

        sb.append("}");
    }

    private static void toJSON(String name, Object src, Appender sb, Serializer serializer) {
        if (name != null)
            sb.append("\"").append(name).append("\"").append(":");
        switch (serializer) {
            case NUMBER_BOOLEAN: {
                sb.append(src);
                break;
            }
            case STRING: {
                sb.append("\"");
                JsonObject.escape(String.valueOf(src), sb);
                sb.append("\"");
                break;
            }
            case COLLECTION: {
                Iterator i = ((Collection) src).iterator();
                sb.append("[");
                boolean comma = false;
                while (i.hasNext()) {
                    if (comma) sb.append(",");
                    toJSON(i.next(), sb);
                    comma = true;
                }
                sb.append("]");
                break;
            }
            case MAP: {
                sb.append("{");
                boolean comma = false;
                for (Object ob : ((Map) src).entrySet()) {
                    if (comma) sb.append(",");
                    Map.Entry entry = (Map.Entry) ob;
                    sb.append("\"").append(entry.getKey()).append("\":");
                    toJSON(entry.getValue(), sb);
                    comma = true;
                }
                sb.append("}");
                break;
            }
            case ARRAY: {
                int length = Array.getLength(src);
                sb.append("[");
                for (int i = 0; i < length; i++) {
                    if (i > 0) sb.append(",");
                    toJSON(Array.get(src, i), sb);
                }
                sb.append("]");
                break;
            }
            case DATE: {
                sb.append("\"").append(src).append("\"");
                break;
            }
            case OBJECT: {
                toJSON(src, sb);
                break;
            }
            case ENUM: {
                sb.append("\"").append(src).append("\"");
                break;
            }
        }
    }
}
