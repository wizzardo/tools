package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.DateIso8601;
import com.wizzardo.tools.misc.UncheckedThrow;
import com.wizzardo.tools.reflection.FieldReflection;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: moxa
 * Date: 3/23/13
 */
class Binder {

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

        public void serialize(Object parent, FieldReflection field, Appender appender, Generic generic) {
            checkNullAndSerialize(field.getObject(parent), appender, generic);
        }

        abstract public void serialize(Object object, Appender appender, Generic generic);
    }

    private static abstract class PrimitiveSerializer extends Serializer {
        protected PrimitiveSerializer() {
            super(SerializerType.NUMBER_BOOLEAN);
        }

        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            throw new IllegalStateException("PrimitiveSerializer can serialize only primitives");
        }

        public abstract void serialize(Object parent, FieldReflection field, Appender appender, Generic generic);
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
            Generic inner = null;

            if (generic != null && generic.typeParameters.length == 1)
                inner = generic.typeParameters[0];

            sb.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(',');
                serializer.checkNullAndSerialize(arr[i], sb, inner);
            }
            sb.append(']');
        }
    }

    private static PrimitiveSerializer intSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, Generic generic) {
            appender.append(field.getInteger(parent));
        }
    };
    private static PrimitiveSerializer longSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, Generic generic) {
            appender.append(field.getLong(parent));
        }
    };
    private static PrimitiveSerializer shortSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, Generic generic) {
            appender.append(field.getShort(parent));
        }
    };
    private static PrimitiveSerializer byteSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, Generic generic) {
            appender.append(field.getByte(parent));
        }
    };
    private static PrimitiveSerializer charSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, Generic generic) {
            appender.append('"');
            appender.append(field.getChar(parent));
            appender.append('"');
        }
    };
    private static PrimitiveSerializer booleanSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, Generic generic) {
            appender.append(field.getBoolean(parent));
        }
    };
    private static PrimitiveSerializer floatSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, Generic generic) {
            appender.append(field.getFloat(parent));
        }
    };
    private static PrimitiveSerializer doubleSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, Generic generic) {
            appender.append(field.getDouble(parent));
        }
    };

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
    private static Serializer simpleSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appender.append(String.valueOf(object));
        }
    };
    private static Serializer intNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appender.append(((Number) object).intValue());
        }
    };
    private static Serializer longNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appender.append(((Number) object).longValue());
        }
    };
    private static Serializer floatNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appender.append(((Number) object).floatValue());
        }
    };
    private static Serializer doubleNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, Generic generic) {
            appender.append(((Number) object).doubleValue());
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
                appender.append(arr[i]);
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
                appender.append(arr[i]);
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
                appender.append(arr[i]);
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
                appender.append(arr[i]);
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
                appender.append(arr[i]);
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
                appender.append(arr[i]);
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
                appender.append(arr[i]);
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
            Map<String, FieldInfo> list;
            if (generic != null && src.getClass() == generic.clazz)
                list = generic.getFields();
            else
                list = getFields(src.getClass());

            for (FieldInfo info : list.values()) {
                Field field = info.field;
                if (comma)
                    sb.append(',');
                else
                    comma = true;

                appendName(field.getName(), sb, false);
                info.serializer.serialize(src, info.setter, sb, info.generic);
            }
            sb.append('}');
        }
    };
    private static Serializer simpleBoxedSerializer = new ArrayBoxedSerializer(simpleSerializer);
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
        else if (clazz.isPrimitive() || Boolean.class == clazz || Number.class.isAssignableFrom(clazz) || Character.class == clazz) {
            if (clazz.isPrimitive()) {
                if (clazz == int.class)
                    return intSerializer;
                if (clazz == long.class)
                    return longSerializer;
                if (clazz == byte.class)
                    return byteSerializer;
                if (clazz == short.class)
                    return shortSerializer;
                if (clazz == char.class)
                    return charSerializer;
                if (clazz == float.class)
                    return floatSerializer;
                if (clazz == double.class)
                    return doubleSerializer;
                if (clazz == boolean.class)
                    return booleanSerializer;
            }
            if (clazz == Integer.class || clazz == Byte.class || clazz == Short.class)
                return intNumberSerializer;
            if (clazz == Long.class)
                return longNumberSerializer;
            if (clazz == Float.class)
                return floatNumberSerializer;
            if (clazz == Double.class)
                return doubleNumberSerializer;
            if (clazz == Character.class)
                return characterSerializer;

            return simpleSerializer;
        } else if (Collection.class.isAssignableFrom(clazz))
            return collectionSerializer;
        else if (Map.class.isAssignableFrom(clazz))
            return mapSerializer;
        else if (Date.class.isAssignableFrom(clazz))
            return dateSerializer;
        else if (Array.class == clazz || clazz.isArray()) {
            clazz = getArrayType(clazz);
            if (clazz != null) {
                if (clazz.isPrimitive()) {
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
                } else {
                    if (clazz == Float.class ||
                            clazz == Double.class ||
                            clazz == Byte.class ||
                            clazz == Short.class ||
                            clazz == Integer.class ||
                            clazz == Long.class ||
                            clazz == Boolean.class)
                        return simpleBoxedSerializer;
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
            }
            return arraySerializer;
        } else if (clazz.isEnum())
            return enumSerializer;
        else
            return objectSerializer;
    }

    static Object createObject(Class clazz) {
        Constructor c = cachedConstructors.get(clazz);
        if (c == null) {
            c = initDefaultConstructor(clazz);
            c.setAccessible(true);
        }

        return createInstance(c);
    }


    static Collection createCollection(Class clazz) {
        Constructor c = cachedConstructors.get(clazz);
        if (c == null) {
            if (clazz == List.class)
                c = initDefaultConstructor(ArrayList.class);
            else if (clazz == Set.class)
                c = initDefaultConstructor(HashSet.class);
            else
                c = initDefaultConstructor(clazz);
        }
        return (Collection) createInstance(c);
    }

    static Map createMap(Class clazz) {
        Constructor c = cachedConstructors.get(clazz);
        if (c == null) {
            if (clazz == Map.class)
                c = initDefaultConstructor(HashMap.class);
            else
                c = initDefaultConstructor(clazz);
        }
        return (Map) createInstance(c);
    }

    private static Constructor initDefaultConstructor(Class clazz) {
        Constructor c;
        try {
            c = clazz.getDeclaredConstructor();
            cachedConstructors.put(clazz, c);
        } catch (NoSuchMethodException e) {
            throw UncheckedThrow.rethrow(e);
        }
        return c;
    }

    private static Object createInstance(Constructor c) {
        try {
            return c.newInstance();
        } catch (IllegalAccessException e) {
            throw UncheckedThrow.rethrow(e);
        } catch (InstantiationException e) {
            throw UncheckedThrow.rethrow(e);
        } catch (InvocationTargetException e) {
            throw UncheckedThrow.rethrow(e);
        }
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

    static void toJSON(Object src, Appender sb) {
        if (src == null) {
            nullSerializer.serialize(null, sb, null);
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

    private static void appendString(Object ob, Appender sb) {
        sb.append('"');
        JsonTools.escape((String) ob, sb);
        sb.append('"');
    }

    private static void appendCollection(Object src, Appender sb, Generic generic) {
        Serializer serializer = null;
        Generic inner = null;
        if (generic != null && generic.typeParameters.length == 1) {
            serializer = generic.typeParameters[0].serializer;
            inner = generic.typeParameters[0];
        }

        sb.append('[');
        boolean comma = false;
        if (serializer != null)
            for (Object ob : (Collection) src) {
                if (comma)
                    sb.append(',');
                else
                    comma = true;
                serializer.checkNullAndSerialize(ob, sb, inner);
            }
        else
            for (Object ob : (Collection) src) {
                if (comma)
                    sb.append(',');
                else
                    comma = true;
                toJSON(ob, sb);
            }
        sb.append(']');
    }

    private static void appendArray(Object src, Appender sb, Generic generic) {
        Object[] arr = (Object[]) src;
        int length = arr.length;

        Serializer serializer = null;
        Generic inner = null;
        if (generic != null && generic.typeParameters.length == 1) {
            serializer = generic.typeParameters[0].serializer;
            inner = generic.typeParameters[0];
        } else if (getArrayType(arr.getClass()) != Object.class)
            serializer = classToSerializer(getArrayType(arr.getClass()));

        sb.append('[');
        if (serializer != null)
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(',');
                serializer.checkNullAndSerialize(arr[i], sb, inner);
            }
        else
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(',');
                toJSON(arr[i], sb);
            }
        sb.append(']');
    }

    private static void appendMap(Object src, Appender sb, Generic generic) {
        Serializer serializer = null;
        Generic inner = null;
        if (generic != null && generic.typeParameters.length == 2) {
            serializer = generic.typeParameters[1].serializer;
            inner = generic.typeParameters[1];
        }
        sb.append('{');
        boolean comma = false;
        if (serializer != null)
            for (Map.Entry entry : ((Map<?, ?>) src).entrySet()) {
                if (comma)
                    sb.append(',');
                else
                    comma = true;
                appendName(String.valueOf(entry.getKey()), sb, true);
                serializer.checkNullAndSerialize(entry.getValue(), sb, inner);
            }
        else
            for (Map.Entry entry : ((Map<?, ?>) src).entrySet()) {
                if (comma)
                    sb.append(',');
                else
                    comma = true;
                toJSON(String.valueOf(entry.getKey()), entry.getValue(), sb);
            }
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
