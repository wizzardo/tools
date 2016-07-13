package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;
import com.wizzardo.tools.misc.DateIso8601;
import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.Fields;
import com.wizzardo.tools.reflection.Generic;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: moxa
 * Date: 3/23/13
 */
public class Binder {

    protected static final JsonFieldSetterFactory JSON_FIELD_SETTER_FACTORY = new JsonFieldSetterFactory();
    private static Map<Class, JsonFields> cachedFields = new ConcurrentHashMap<Class, JsonFields>();
    private static Map<Class, Constructor> cachedConstructors = new ConcurrentHashMap<Class, Constructor>();
    private static Map<Class, Serializer> serializers = new ConcurrentHashMap<Class, Serializer>();
    static CharTree<String> fieldsNames = new CharTree<String>();

    public static abstract class Serializer {
        static char[] nullArray = new char[]{'n', 'u', 'l', 'l'};
        final SerializerType type;

        protected Serializer(SerializerType type) {
            this.type = type;
        }

        public void checkNullAndSerialize(Object object, Appender appender, JsonGeneric generic) {
            if (object == null)
                appender.append(nullArray);
            else
                serialize(object, appender, generic);

        }

        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic) {
            checkNullAndSerialize(field.getObject(parent), appender, generic);
        }

        abstract public void serialize(Object object, Appender appender, JsonGeneric generic);
    }

    public static abstract class PrimitiveSerializer extends Serializer {
        protected PrimitiveSerializer() {
            super(SerializerType.NUMBER_BOOLEAN);
        }

        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            throw new IllegalStateException("PrimitiveSerializer can serialize only primitives");
        }

        public abstract void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic);
    }

    public static class ArrayBoxedSerializer extends Serializer {
        Serializer serializer;

        protected ArrayBoxedSerializer(Serializer serializer) {
            super(SerializerType.ARRAY);
            this.serializer = serializer;
        }

        @Override
        public void serialize(Object src, Appender sb, JsonGeneric generic) {
            Object[] arr = (Object[]) src;
            int length = arr.length;
            JsonGeneric inner = null;

            if (generic != null && generic.typesCount() == 1)
                inner = generic.type(0);

            sb.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(',');
                serializer.checkNullAndSerialize(arr[i], sb, inner);
            }
            sb.append(']');
        }
    }

    public final static PrimitiveSerializer intSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic) {
            appender.append(field.getInteger(parent));
        }
    };
    public final static PrimitiveSerializer longSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic) {
            appender.append(field.getLong(parent));
        }
    };
    public final static PrimitiveSerializer shortSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic) {
            appender.append(field.getShort(parent));
        }
    };
    public final static PrimitiveSerializer byteSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic) {
            appender.append(field.getByte(parent));
        }
    };
    public final static PrimitiveSerializer charSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic) {
            appender.append('"');
            appender.append(field.getChar(parent));
            appender.append('"');
        }
    };
    public final static PrimitiveSerializer booleanSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic) {
            appender.append(field.getBoolean(parent));
        }
    };
    public final static PrimitiveSerializer floatSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic) {
            appender.append(field.getFloat(parent));
        }
    };
    public final static PrimitiveSerializer doubleSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic) {
            appender.append(field.getDouble(parent));
        }
    };

    public final static Serializer stringSerializer = new Serializer(SerializerType.STRING) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appendString(object, appender);
        }
    };
    public final static Serializer characterSerializer = new Serializer(SerializerType.STRING) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appender.append('"');
            JsonTools.escape((Character) object, appender);
            appender.append('"');
        }
    };
    public final static Serializer simpleSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appender.append(String.valueOf(object));
        }
    };
    public final static Serializer intNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appender.append(((Number) object).intValue());
        }
    };
    public final static Serializer longNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appender.append(((Number) object).longValue());
        }
    };
    public final static Serializer floatNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appender.append(((Number) object).floatValue());
        }
    };
    public final static Serializer doubleNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appender.append(((Number) object).doubleValue());
        }
    };
    public final static Serializer collectionSerializer = new Serializer(SerializerType.COLLECTION) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appendCollection(object, appender, generic);
        }
    };
    public final static Serializer arraySerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appendArray(object, appender, generic);
        }
    };
    public final static Serializer arrayIntSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
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
    public final static Serializer arrayLongSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
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
    public final static Serializer arrayByteSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
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
    public final static Serializer arrayShortSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
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
    public final static Serializer arrayBooleanSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
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
    public final static Serializer arrayFloatSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
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
    public final static Serializer arrayDoubleSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
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
    public final static Serializer arrayCharSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
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
    public final static Serializer mapSerializer = new Serializer(SerializerType.MAP) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appendMap(object, appender, generic);
        }
    };
    public final static Serializer dateSerializer = new Serializer(SerializerType.DATE) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appender.append('"');
            appender.append(DateIso8601.formatToChars((Date) object));
            appender.append('"');
        }
    };
    public final static Serializer enumSerializer = new Serializer(SerializerType.ENUM) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appender.append('"');
            appender.append(object);
            appender.append('"');
        }
    };
    public final static Serializer nullSerializer = new Serializer(SerializerType.NULL) {

        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic) {
            appender.append(nullArray);
        }
    };
    public final static Serializer objectSerializer = new Serializer(SerializerType.OBJECT) {
        @Override
        public void serialize(Object src, Appender sb, JsonGeneric generic) {
            sb.append('{');
            boolean comma = false;
            JsonFields fields;
            if (generic != null && src.getClass() == generic.clazz)
                fields = generic.getFields();
            else
                fields = getFields(src.getClass());

            for (JsonFieldInfo info : fields.fields()) {
                Field field = info.field;
                if (comma)
                    sb.append(',');
                else
                    comma = true;

                appendName(field.getName(), sb, false);
                info.serializer.serialize(src, info.reflection, sb, info.generic);
            }
            sb.append('}');
        }
    };
    public final static Serializer genericSerializer = new Serializer(SerializerType.OBJECT) {
        @Override
        public void serialize(Object src, Appender sb, JsonGeneric generic) {
            classToSerializer(src.getClass()).serialize(src, sb, null);
        }
    };
    public final static Fields.Mapper<Field, JsonFieldInfo> JSON_FIELD_INFO_MAPPER = new Fields.Mapper<Field, JsonFieldInfo>() {
        @Override
        public JsonFieldInfo map(Field field) {
            if (!fieldsNames.contains(field.getName()))
                fieldsNames.append(field.getName(), field.getName());
            if (field.getGenericType() != field.getType())
                return new JsonFieldInfo(field, JSON_FIELD_SETTER_FACTORY.create(field, true), genericSerializer);
            else
                return new JsonFieldInfo(field, JSON_FIELD_SETTER_FACTORY.create(field, true), getReturnType(field));
        }
    };
    public final static Serializer simpleBoxedSerializer = new ArrayBoxedSerializer(simpleSerializer);
    public final static Serializer stringArraySerializer = new ArrayBoxedSerializer(stringSerializer);
    public final static Serializer charArraySerializer = new ArrayBoxedSerializer(characterSerializer);
    public final static Serializer dateArraySerializer = new ArrayBoxedSerializer(dateSerializer);
    public final static Serializer enumArraySerializer = new ArrayBoxedSerializer(enumSerializer);
    public final static Serializer collectionArraySerializer = new ArrayBoxedSerializer(collectionSerializer);
    public final static Serializer mapArraySerializer = new ArrayBoxedSerializer(mapSerializer);
    public final static Serializer arrayArraySerializer = new ArrayBoxedSerializer(arraySerializer);

    enum SerializerType {
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

    static JsonBinder getObjectBinder(JsonGeneric generic) {
        if (generic == null || generic.clazz == null)
            return new JsonObjectBinder();
        else if (Map.class.isAssignableFrom(generic.clazz))
            return new JavaMapBinder(generic);
        else
            return new JavaObjectBinder(generic);
    }

    static JsonBinder getArrayBinder(JsonGeneric generic) {
        if (generic == null || generic.clazz == null)
            return new JsonArrayBinder();
        else
            return new JavaArrayBinder(generic);
    }

    public static JsonFields getFields(Class clazz) {
        JsonFields fields = cachedFields.get(clazz);
        if (fields == null) {
            synchronized (clazz) {
                fields = cachedFields.get(clazz);
                if (fields != null)
                    return fields;

                fields = new JsonFields(clazz, JSON_FIELD_INFO_MAPPER);
                cachedFields.put(clazz, fields);
            }
        }
        return fields;
    }

    public static JsonFieldInfo getField(Class clazz, String key) {
        return getFields(clazz).get(key);
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
                c = initDefaultConstructor(List.class, ArrayList.class);
            else if (clazz == Set.class)
                c = initDefaultConstructor(Set.class, HashSet.class);
            else
                c = initDefaultConstructor(clazz);
        }
        return (Collection) createInstance(c);
    }

    static Map createMap(Class clazz) {
        Constructor c = cachedConstructors.get(clazz);
        if (c == null) {
            if (clazz == Map.class)
                c = initDefaultConstructor(Map.class, HashMap.class);
            else
                c = initDefaultConstructor(clazz);
        }
        return (Map) createInstance(c);
    }

    private static Constructor initDefaultConstructor(Class clazz) {
        return initDefaultConstructor(clazz, clazz);
    }

    private static Constructor initDefaultConstructor(Class classKey, Class classReal) {
        Constructor c;
        try {
            c = classReal.getDeclaredConstructor();
            cachedConstructors.put(classKey, c);
        } catch (NoSuchMethodException e) {
            throw Unchecked.rethrow(e);
        }
        return c;
    }

    private static Object createInstance(Constructor c) {
        try {
            return c.newInstance();
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        } catch (InstantiationException e) {
            throw Unchecked.rethrow(e);
        } catch (InvocationTargetException e) {
            throw Unchecked.rethrow(e);
        }
    }

    static Object createArrayByComponentType(Class clazz, int size) {
        return Array.newInstance(clazz, size);
    }

    static Object createArray(Generic generic, int size) {
        return createArrayByComponentType(generic.type(0).clazz, size);
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

    private static void appendCollection(Object src, Appender sb, JsonGeneric generic) {
        Serializer serializer = null;
        JsonGeneric inner = null;
        if (generic != null && generic.typesCount() == 1) {
            serializer = generic.type(0).serializer;
            inner = generic.type(0);
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

    private static void appendArray(Object src, Appender sb, JsonGeneric generic) {
        Object[] arr = (Object[]) src;
        int length = arr.length;

        Serializer serializer = null;
        JsonGeneric inner = null;
        if (generic != null && generic.typesCount() == 1) {
            serializer = generic.type(0).serializer;
            inner = generic.type(0);
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

    private static void appendMap(Object src, Appender sb, JsonGeneric generic) {
        Serializer serializer = null;
        JsonGeneric inner = null;
        if (generic != null && generic.typesCount() == 2) {
            serializer = generic.type(1).serializer;
            inner = generic.type(1);
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
