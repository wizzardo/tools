package com.wizzardo.tools.json;

import com.wizzardo.tools.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: moxa
 * Date: 3/23/13
 */
public class Binder {

    private static Map<Class, Map<String, Pair<Field, Serializer>>> cachedFields = new ConcurrentHashMap<Class, Map<String, Pair<Field, Serializer>>>();
    private static Map<Class, Constructor> cachedConstructors = new ConcurrentHashMap<Class, Constructor>();
//    private static Map<Class, List<Pair<Method, Serializer>>> cachedGetters = new ConcurrentHashMap<Class, List<Pair<Method, Serializer>>>();

    enum Serializer {
        STRING, NUMBER_BOOLEAN, COLLECTION, ARRAY, MAP, DATE, OBJECT
    }


    static <T> T createInstance(Class<T> clazz, Type generic) {
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

    public static Pair<Field, Serializer> getField(Class clazz, String key) {
        Map<String, Pair<Field, Serializer>> fields = cachedFields.get(clazz);
        if (fields == null) {
            fields = new HashMap<String, Pair<Field, Serializer>>();
            Class cl = clazz;
            while (cl != null) {
                Field[] ff = cl.getDeclaredFields();
                for (Field field : ff) {
//                    System.out.println("field " + field);
                    if (
                            !Modifier.isTransient(field.getModifiers())
                                    && !Modifier.isStatic(field.getModifiers())
                                    && !Modifier.isFinal(field.getModifiers())
//                                    && !Modifier.isPrivate(field.getModifiers())
//                                    && !Modifier.isProtected(field.getModifiers())
                            ) {
//                        System.out.println("add field " + field);
                        field.setAccessible(true);
                        fields.put(field.getName(), new Pair<Field, Serializer>(field, getReturnType(field)));
                    }
                }
                cl = cl.getSuperclass();
            }
            cachedFields.put(clazz, fields);
        }
        return fields.get(key);
    }

    public static void setValue(Object object, String key, Object value) {
        Class clazz = object.getClass();

        Pair<Field, Serializer> pair = getField(clazz, key);
        if (pair == null)
            return;

        Field field = pair.key;
        Serializer s = pair.value;

        try {
            switch (s) {
                case STRING:
                case NUMBER_BOOLEAN:
                    field.set(object, JsonItem.getAs(value, field.getType()));
                    break;
//                case ARRAY:
//                    List l = (List) value;
//                    Object array = createArray(field.getType(), l.size());
//                    Class type = getArrayType(field.getType());
//                    for (int i = 0; i < l.size(); i++) {
//                        Array.set(array, i, JsonItem.getAs(l.get(i), type));
//                    }
//                    field.set(object, array);
//                    break;
//            case COLLECTION:
//                field.set(object, fromJSON(field.getType(), value, field.getGenericType()));
//                break;
                default:
                    field.set(object, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

                Map<String, Pair<Field, Serializer>> list = cachedFields.get(clazz);
                if (list == null) {
                    list = new HashMap<String, Pair<Field, Serializer>>();
                    Class cl = clazz;
                    while (cl != null) {
                        Field[] ff = cl.getDeclaredFields();
                        for (Field field : ff) {
//                    System.out.println("field " + field);
                            if (
                                    !Modifier.isTransient(field.getModifiers())
                                            && !Modifier.isStatic(field.getModifiers())
                                            && !Modifier.isFinal(field.getModifiers())
//                                    && !Modifier.isPrivate(field.getModifiers())
//                                    && !Modifier.isProtected(field.getModifiers())
                                    ) {
//                        System.out.println("add field " + field);
                                field.setAccessible(true);
                                list.put(field.getName(), new Pair<Field, Serializer>(field, getReturnType(field)));
                            }
                        }
                        cl = cl.getSuperclass();
                    }
                    cachedFields.put(clazz, list);
                }

                for (Pair<Field, Serializer> pair : list.values()) {
                    Field field = pair.key;
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

    public static String toJSON(Object src) {
        StringBuilder sb = new StringBuilder();
        toJSON(src, sb);
        return sb.toString();
    }

    private static void toJSON(Object src, StringBuilder sb) {
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
        Map<String, Pair<Field, Serializer>> list = cachedFields.get(src.getClass());
        if (list == null) {
            Class clazz = src.getClass();
            list = new HashMap<String, Pair<Field, Serializer>>();
            cachedFields.put(clazz, list);
            while (clazz != null) {
                Field[] ff = clazz.getDeclaredFields();
                for (Field field : ff) {
//                    System.out.println("field " + field);
                    if (!fields.contains(field.getName())
                            && !Modifier.isTransient(field.getModifiers())
                            && !Modifier.isStatic(field.getModifiers())
//                            && !Modifier.isPrivate(field.getModifiers())
//                            && !Modifier.isProtected(field.getModifiers())
                            ) {
//                        System.out.println("add field " + field);
                        field.setAccessible(true);
                        list.put(field.getName(), new Pair<Field, Serializer>(field, getReturnType(field)));
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }

        for (Pair<Field, Serializer> pair : list.values()) {
            Field field = pair.key;
            try {
                if (comma)
                    sb.append(",");
                toJSON(field.getName(), field.get(src), sb, pair.value);
                fields.add(field.getName());
                comma = true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

//        List<Pair<Method, Serializer>> getters = cachedGetters.get(src.getClass());
//        if (getters == null) {
//            getters = new ArrayList<Pair<Method, Serializer>>();
//
//            outer:
//            for (Method method : src.getClass().getMethods()) {
//                if (isGetter(method, src.getClass())) {
//                    System.out.println("getter: " + method);
//                    String field;
//                    if (method.getName().startsWith("get"))
//                        field = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
//                    else // is ?
//                        field = method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3);
//
//                    Field f = null;
//                    {
//                        Class clazz = src.getClass();
//                        while (f == null && clazz != null)
//                            try {
//                                f = clazz.getDeclaredField(field);
//                                if (Modifier.isTransient(f.getModifiers())) {
//                                    continue outer;
//                                }
//                            } catch (NoSuchFieldException e) {
//                                clazz = clazz.getSuperclass();
//                            }
//                    }
//
//                    if (f != null && !fields.contains(field)) {
//                        getters.add(new Pair<Method, Serializer>(method, getReturnType(method)));
//                    }
//                }
//            }
//            cachedGetters.put(src.getClass(), getters);
//        }
//
//        for (Pair<Method, Serializer> pair : getters) {
//            Method method = pair.key;
//            String field;
//            if (method.getName().startsWith("get"))
//                field = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
//            else // is ?
//                field = method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3);
//
//            if (!fields.contains(field)) {
//                try {
//                    if (comma)
//                        sb.append(",");
//                    toJSON(field, method.invoke(src, null), sb, pair.value);
//                    fields.add(field);
//                    comma = true;
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//            }
//        }
        sb.append("}");
    }

    private static void toJSON(String name, Object src, StringBuilder sb, Serializer serializer) {
        if (name != null)
            sb.append("\"").append(name).append("\"").append(":");
        switch (serializer) {
            case NUMBER_BOOLEAN: {
                sb.append(src);
                break;
            }
            case STRING: {
                sb.append("\"").append(escapeJsonString(String.valueOf(src))).append("\"");
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
//                    if (entry.getValue().getClass() == String.class)
//                        sb.append("\"").append(entry.getValue()).append("\"");
//                    else
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
        }
    }

    public static String escapeJsonString(String str) {
        StringBuilder out = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\b':
                    out.append("\\b");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '/':
                    out.append("\\/");
                    break;
                case '"':
                    out.append("\\\"");
                    break;
                default:
                    out.append(c);
                    break;
            }
        }
        return out.toString();
    }
}
