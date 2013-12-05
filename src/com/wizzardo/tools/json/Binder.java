package com.wizzardo.tools.json;

import com.wizzardo.tools.Pair;
import sun.org.mozilla.javascript.internal.WrappedException;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: moxa
 * Date: 3/23/13
 */
public class Binder {

    private static Map<Class, List<Pair<Field, Serializer>>> cachedFields = new ConcurrentHashMap<Class, List<Pair<Field, Serializer>>>();
    private static Map<Class, Constructor> cachedConstructors = new ConcurrentHashMap<Class, Constructor>();
//    private static Map<Class, List<Pair<Method, Serializer>>> cachedGetters = new ConcurrentHashMap<Class, List<Pair<Method, Serializer>>>();

    private enum Serializer {
        STRING, NUMBER_BOOLEAN, COLLECTION, ARRAY, MAP, DATE, OBJECT
    }

    private static Serializer getReturnType(Method method) {
        return classToSerializer(method.getReturnType());
    }

    private static Serializer getReturnType(Field field) {
        return classToSerializer(field.getType());
    }

    private static Serializer classToSerializer(Class clazz) {
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

                List<Pair<Field, Serializer>> list = cachedFields.get(clazz);
                if (list == null) {
                    list = new ArrayList<Pair<Field, Serializer>>();
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
                                list.add(new Pair<Field, Serializer>(field, getReturnType(field)));
                            }
                        }
                        cl = cl.getSuperclass();
                    }
                    cachedFields.put(clazz, list);
                }

                for (Pair<Field, Serializer> pair : list) {
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

    private static Collection createCollection(Class clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Constructor c = cachedConstructors.get(clazz);
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
    }

    private static Object createArray(Class clazz, int size) {
        if (clazz == int[].class) {
            return new int[size];
        }
        if (clazz == String[].class) {
            return new String[size];
        }
        if (clazz == double[].class) {
            return new double[size];
        }
        if (clazz == long[].class) {
            return new long[size];
        }
        if (clazz == boolean[].class) {
            return new boolean[size];
        }
        if (clazz == char[].class) {
            return new char[size];
        }
        if (clazz == float[].class) {
            return new float[size];
        }
        if (clazz == byte[].class) {
            return new byte[size];
        }
        if (clazz == short[].class) {
            return new short[size];
        }
        ClassLoader cl = clazz.getClassLoader();
        String name = clazz.getName();
        name = name.substring(2, name.length() - 1);
        try {
            Class clazz2 = cl.loadClass(name);
            return Array.newInstance(clazz2, size);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class getArrayType(Class clazz) {
        if (clazz == int[].class) {
            return int.class;
        }
        if (clazz == String[].class) {
            return String.class;
        }
        if (clazz == double[].class) {
            return double.class;
        }
        if (clazz == long[].class) {
            return long.class;
        }
        if (clazz == boolean[].class) {
            return boolean.class;
        }
        if (clazz == char[].class) {
            return char.class;
        }
        if (clazz == float[].class) {
            return float.class;
        }
        if (clazz == byte[].class) {
            return byte.class;
        }
        if (clazz == short[].class) {
            return short.class;
        }
        ClassLoader cl = clazz.getClassLoader();
        String name = clazz.getName();
        name = name.substring(2, name.length() - 1);
        try {
            return cl.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new WrappedException(e);
        }
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
        List<Pair<Field, Serializer>> list = cachedFields.get(src.getClass());
        if (list == null) {
            Class clazz = src.getClass();
            list = new ArrayList<Pair<Field, Serializer>>();
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
                        list.add(new Pair<Field, Serializer>(field, getReturnType(field)));
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }

        for (Pair<Field, Serializer> pair : list) {
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
