package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.*;
import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.Fields;
import com.wizzardo.tools.reflection.Generic;

import java.lang.reflect.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: moxa
 * Date: 3/23/13
 */
public class Binder {

    public final static Fields.FieldMapper<JsonFieldInfo, JsonGeneric> JSON_FIELD_INFO_MAPPER = new Fields.FieldMapper<JsonFieldInfo, JsonGeneric>() {
        @Override
        public JsonFieldInfo map(Field field, JsonGeneric generic) {
            if (!fieldsNames.contains(field.getName()))
                fieldsNames.append(field.getName(), Pair.of(field.getName(), (JsonFieldInfo) null));
            field.setAccessible(true);
            Class<?> type = field.getType();
            Map<String, JsonGeneric> types = generic.types();
            if (field.getGenericType() != type && field.getGenericType() instanceof TypeVariable) {
                Generic g = types.get(((TypeVariable) field.getGenericType()).getName());
                if (g != null)
                    type = g.clazz;
            }

            return new JsonFieldInfo(field,
                    JSON_FIELD_SETTER_FACTORY.create(field, type),
                    generic.create(field.getGenericType(), types),
                    classToSerializer(type));
        }
    };
    protected static final Object[] EMPTY_ARRAY = new Object[0];
    public static SerializationContext DEFAULT_SERIALIZATION_CONTEXT = new SerializationContext();
    protected static final JsonFieldSetterFactory JSON_FIELD_SETTER_FACTORY = new JsonFieldSetterFactory();
    private static Map<Class, Constructor> cachedConstructors = new ConcurrentHashMap<Class, Constructor>();
    private static Map<Class, Serializer> serializers = new ConcurrentHashMap<Class, Serializer>();
    static CharTree<Pair<String, JsonFieldInfo>> fieldsNames = new CharTree<Pair<String, JsonFieldInfo>>();

//    static <T> JsonGeneric<T> getGeneric(Class<T> clazz) {
//        return DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz);
//    }
//
//    static <T> JsonGeneric<T> getGeneric(Class<T> clazz, Class... generic) {
//        return DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz, generic);
//    }
//
//    static <T> JsonGeneric<T> getGeneric(Class<T> clazz, JsonGeneric... generic) {
//        return DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz, generic);
//    }

    public static JsonFields getFields(Class clazz) {
        return DEFAULT_SERIALIZATION_CONTEXT.getFields(clazz);
    }

    public static JsonFields getFields(JsonGeneric generic) {
        return DEFAULT_SERIALIZATION_CONTEXT.getFields(generic);
    }

    public static JsonFieldInfo getField(Class clazz, String key) {
        return getFields(clazz).get(key);
    }

    public static abstract class Serializer {
        static char[] nullArray = new char[]{'n', 'u', 'l', 'l'};
        final SerializerType type;

        protected Serializer(SerializerType type) {
            this.type = type;
        }

        public void checkNullAndSerialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            if (object == null)
                appender.append(nullArray);
            else
                serialize(object, appender, generic, context);

        }

        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context) {
            checkNullAndSerialize(field.getObject(parent), appender, generic, context);
        }

        abstract public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context);
    }

    public static abstract class PrimitiveSerializer extends Serializer {
        protected PrimitiveSerializer() {
            super(SerializerType.NUMBER_BOOLEAN);
        }

        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            throw new IllegalStateException("PrimitiveSerializer can serialize only primitives");
        }

        public abstract void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context);
    }

    public static class ArrayBoxedSerializer extends Serializer {
        Serializer serializer;

        protected ArrayBoxedSerializer(Serializer serializer) {
            super(SerializerType.ARRAY);
            this.serializer = serializer;
        }

        @Override
        public void serialize(Object src, Appender sb, JsonGeneric generic, SerializationContext context) {
            Object[] arr = (Object[]) src;
            int length = arr.length;
            JsonGeneric inner = null;

            if (generic != null && generic.typesCount() == 1)
                inner = generic.type(0);

            sb.append('[');
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(',');
                serializer.checkNullAndSerialize(arr[i], sb, inner, context);
            }
            sb.append(']');
        }
    }

    public final static PrimitiveSerializer intSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(field.getInteger(parent));
        }
    };
    public final static PrimitiveSerializer longSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(field.getLong(parent));
        }
    };
    public final static PrimitiveSerializer shortSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(field.getShort(parent));
        }
    };
    public final static PrimitiveSerializer byteSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(field.getByte(parent));
        }
    };
    public final static PrimitiveSerializer charSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            appender.append(field.getChar(parent));
            appender.append('"');
        }
    };
    public final static PrimitiveSerializer booleanSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(field.getBoolean(parent));
        }
    };
    public final static PrimitiveSerializer floatSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(field.getFloat(parent));
        }
    };
    public final static PrimitiveSerializer doubleSerializer = new PrimitiveSerializer() {
        @Override
        public void serialize(Object parent, FieldReflection field, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(field.getDouble(parent));
        }
    };

    public final static Serializer stringSerializer = new Serializer(SerializerType.STRING) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appendString(object, appender);
        }
    };
    public final static Serializer characterSerializer = new Serializer(SerializerType.STRING) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            JsonTools.escape((Character) object, appender);
            appender.append('"');
        }
    };
    public final static Serializer simpleSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(String.valueOf(object));
        }
    };
    public final static Serializer intNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            try {
                appender.append(((Number) object).intValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public final static Serializer longNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(((Number) object).longValue());
        }
    };
    public final static Serializer floatNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(((Number) object).floatValue());
        }
    };
    public final static Serializer doubleNumberSerializer = new Serializer(SerializerType.NUMBER_BOOLEAN) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(((Number) object).doubleValue());
        }
    };
    public final static Serializer collectionSerializer = new Serializer(SerializerType.COLLECTION) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appendCollection(object, appender, generic, context);
        }
    };
    public final static Serializer arraySerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appendArray(object, appender, generic, context);
        }
    };
    public final static Serializer arrayIntSerializer = new Serializer(SerializerType.ARRAY) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
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
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
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
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
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
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
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
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
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
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
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
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
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
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
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
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appendMap(object, appender, generic, context);
        }
    };
    public final static Serializer dateSerializer = new Serializer(SerializerType.DATE) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            appender.append((Date) object);
            appender.append('"');
        }
    };
    public final static Serializer localDateTimeSerializer = new Serializer(SerializerType.DATE) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            appender.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format((Temporal) object));
            appender.append('"');
        }
    };
    public final static Serializer localDateSerializer = new Serializer(SerializerType.DATE) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            appender.append(DateTimeFormatter.ISO_LOCAL_DATE.format((Temporal) object));
            appender.append('"');
        }
    };
    public final static Serializer localTimeSerializer = new Serializer(SerializerType.DATE) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            appender.append(DateTimeFormatter.ISO_LOCAL_TIME.format((Temporal) object));
            appender.append('"');
        }
    };
    public final static Serializer offsetDateTimeSerializer = new Serializer(SerializerType.DATE) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            appender.append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format((Temporal) object));
            appender.append('"');
        }
    };
    public final static Serializer offsetTimeSerializer = new Serializer(SerializerType.DATE) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            appender.append(DateTimeFormatter.ISO_OFFSET_TIME.format((Temporal) object));
            appender.append('"');
        }
    };
    public final static Serializer zonedDateTimeSerializer = new Serializer(SerializerType.DATE) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            appender.append(DateTimeFormatter.ISO_ZONED_DATE_TIME.format((Temporal) object));
            appender.append('"');
        }
    };
    public final static Serializer enumSerializer = new Serializer(SerializerType.ENUM) {
        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append('"');
            appender.append(object);
            appender.append('"');
        }
    };
    public final static Serializer nullSerializer = new Serializer(SerializerType.NULL) {

        @Override
        public void serialize(Object object, Appender appender, JsonGeneric generic, SerializationContext context) {
            appender.append(nullArray);
        }
    };
    public final static Serializer objectSerializer = new Serializer(SerializerType.OBJECT) {
        @Override
        public void serialize(Object src, Appender sb, JsonGeneric generic, SerializationContext context) {
//            boolean comma = false;
            JsonFields fields;
            if (generic != null && src.getClass() == generic.clazz)
                fields = generic.getFields();
            else
                fields = context.getFields(src.getClass());

            if (fields.size() == 0)
                sb.append('{');

            for (JsonFieldInfo info : fields.fields()) {
//                Field field = info.field;
//                if (comma)
//                    sb.append(',');
//                else
//                    comma = true;

//                appendName(field.getName(), sb, false);
                sb.append(info.getPreparedFieldName());
                info.serializer.serialize(src, info.reflection, sb, info.generic, context);
            }
            sb.append('}');
        }
    };
    public final static Serializer genericSerializer = new Serializer(SerializerType.OBJECT) {
        @Override
        public void serialize(Object src, Appender sb, JsonGeneric generic, SerializationContext context) {
            Class<?> clazz = src.getClass();
            if (clazz == Object.class)
                objectSerializer.serialize(src, sb, null, context);
            else
                context.getGeneric(clazz).serializer.serialize(src, sb, null, context);
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

    static Serializer getReturnType(Field field) {
        return classToSerializer(field.getType());
    }

    static Serializer classToSerializer(Class clazz) {
        if (clazz == Object.class)
            return genericSerializer;

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
        else if (Temporal.class.isAssignableFrom(clazz)) {
            if (clazz == LocalDateTime.class)
                return localDateTimeSerializer;
            if (clazz == LocalDate.class)
                return localDateSerializer;
            if (clazz == LocalTime.class)
                return localTimeSerializer;
            if (clazz == OffsetDateTime.class)
                return offsetDateTimeSerializer;
            if (clazz == OffsetTime.class)
                return offsetTimeSerializer;
            if (clazz == ZonedDateTime.class)
                return zonedDateTimeSerializer;

            return objectSerializer;
        } else if (Array.class == clazz || clazz.isArray()) {
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
                return new ArrayList();
            if (clazz == Set.class)
                return new HashSet();
            if (clazz == ArrayList.class)
                return new ArrayList();
            if (clazz == HashSet.class)
                return new HashSet();
            if (clazz == LinkedList.class)
                return new LinkedList();
            if (clazz == TreeSet.class)
                return new TreeSet();

            c = initDefaultConstructor(clazz);
        }
        return (Collection) createInstance(c);
    }

    static Map createMap(Class clazz) {
        Constructor c = cachedConstructors.get(clazz);
        if (c == null) {
            if (clazz == Map.class)
                return new HashMap();
            if (clazz == HashMap.class)
                return new HashMap();
            if (clazz == TreeMap.class)
                return new TreeMap();

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
            return c.newInstance(EMPTY_ARRAY);
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
        toJSON(src, sb, DEFAULT_SERIALIZATION_CONTEXT);
    }

    static void toJSON(Object src, Appender sb, SerializationContext context) {
        if (src == null) {
            nullSerializer.serialize(null, sb, null, context);
            return;
        }

        context.getGeneric(src.getClass()).serializer.serialize(src, sb, null, context);
    }

    private static void toJSON(String name, Object src, Appender sb, SerializationContext context) {
        Serializer serializer;
        if (src != null)
            serializer = context.getGeneric(src.getClass()).serializer;
        else
            serializer = nullSerializer;
        appendName(name, sb, true);
        serializer.serialize(src, sb, null, context);
    }

    private static void appendString(Object ob, Appender sb) {
        sb.append('"');
        JsonTools.escape((String) ob, sb);
        sb.append('"');
    }

    private static void appendCollection(Object src, Appender sb, JsonGeneric generic, SerializationContext context) {
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
                serializer.checkNullAndSerialize(ob, sb, inner, context);
            }
        else
            for (Object ob : (Collection) src) {
                if (comma)
                    sb.append(',');
                else
                    comma = true;
                toJSON(ob, sb, context);
            }
        sb.append(']');
    }

    private static void appendArray(Object src, Appender sb, JsonGeneric generic, SerializationContext context) {
        Object[] arr = (Object[]) src;
        int length = arr.length;

        Serializer serializer = null;
        JsonGeneric inner = null;
        if (generic != null && generic.typesCount() == 1) {
            serializer = generic.type(0).serializer;
            inner = generic.type(0);
        } else if (getArrayType(arr.getClass()) != Object.class)
            serializer = context.getGeneric(getArrayType(arr.getClass())).serializer;

        sb.append('[');
        if (serializer != null)
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(',');
                serializer.checkNullAndSerialize(arr[i], sb, inner, context);
            }
        else
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(',');
                toJSON(arr[i], sb, context);
            }
        sb.append(']');
    }

    private static void appendMap(Object src, Appender sb, JsonGeneric generic, SerializationContext context) {
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
                serializer.checkNullAndSerialize(entry.getValue(), sb, inner, context);
            }
        else
            for (Map.Entry entry : ((Map<?, ?>) src).entrySet()) {
                if (comma)
                    sb.append(',');
                else
                    comma = true;
                toJSON(String.valueOf(entry.getKey()), entry.getValue(), sb, context);
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
