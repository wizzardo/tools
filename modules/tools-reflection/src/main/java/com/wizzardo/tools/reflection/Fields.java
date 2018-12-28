package com.wizzardo.tools.reflection;

import com.wizzardo.tools.interfaces.Consumer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by wizzardo on 31/05/16.
 */
public class Fields<T extends FieldInfo> implements Iterable<T> {

    protected static final int SYNTHETIC = 0x00001000;
    protected static final FieldReflectionFactory FIELD_REFLECTION_FACTORY = new FieldReflectionFactory();
    protected static final FieldMapper<FieldInfo, Generic> DEFAULT_MAPPER = new FieldMapper<FieldInfo, Generic>() {
        @Override
        public FieldInfo<FieldReflection, Generic> map(Field field, Generic generic) {
            return new FieldInfo<FieldReflection, Generic>(field, FIELD_REFLECTION_FACTORY.create(field), generic.types);
        }
    };

    public interface FieldMapper<R, G extends Generic> {
        R map(Field field, G generic);
    }

    protected final Map<String, T> map;
    protected final T[] array;

    public Fields(Map<String, T> map) {
        this.map = new HashMap<String, T>(map.size(), 1);
        this.map.putAll(map);
        array = fill(map.values(), createArray(map.size()));
    }

    public Fields(Class clazz) {
        this(clazz, (FieldMapper<T, Generic>) DEFAULT_MAPPER);
    }

    public Fields(Class clazz, FieldMapper<T, Generic> mapper) {
        this(Generic.of(clazz), mapper);
    }

    public Fields(Generic generic) {
        this(generic, (FieldMapper<T, Generic>) DEFAULT_MAPPER);
    }

    public <G extends Generic> Fields(G g, FieldMapper<T, G> mapper) {
        Map<String, T> fields = readFields(g, new LinkedHashMap<String, T>(), mapper);
        this.map = new LinkedHashMap<String, T>(fields.size(), 1);
        this.map.putAll(fields);
        array = fill(map.values(), createArray(map.size()));
    }

    protected T[] createArray(int size) {
        return (T[]) new FieldInfo[size];
    }


    protected T[] fill(Collection<T> c, T[] array) {
        int i = 0;
        for (T fieldInfo : c) {
            array[i++] = fieldInfo;
        }
        return array;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int position = 0;

            @Override
            public boolean hasNext() {
                return position < array.length;
            }

            @Override
            public T next() {
                return array[position++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove an element of an array.");
            }
        };
    }

    public void each(Consumer<FieldInfo> consumer) {
        for (FieldInfo fieldInfo : array) {
            consumer.consume(fieldInfo);
        }
    }

    public T get(String name) {
        return map.get(name);
    }

    public int size() {
        return map.size();
    }

    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    public static Fields<FieldInfo> getFields(Class clazz) {
        return getFields(clazz, DEFAULT_MAPPER);
    }

    public static Fields<FieldInfo> of(Class clazz) {
        return getFields(clazz, DEFAULT_MAPPER);
    }

    public static <T extends FieldInfo> Fields<T> getFields(Class clazz, FieldMapper<T, Generic> mapper) {
        return new Fields<T>(clazz, mapper);
    }

    protected <T extends FieldInfo, G extends Generic> Map<String, T> readFields(G generic, Map<String, T> fields, FieldMapper<T, G> mapper) {
        if (generic == null)
            return fields;

        readFields((G) generic.parent, fields, mapper);

        Field[] ff = generic.clazz.getDeclaredFields();
        for (Field field : ff) {
            if (!Modifier.isTransient(field.getModifiers())
                    && !Modifier.isStatic(field.getModifiers())
                    && (field.getModifiers() & SYNTHETIC) == 0
//                                    && !Modifier.isFinal(field.getModifiers())
//                                    && !Modifier.isPrivate(field.getModifiers())
//                                    && !Modifier.isProtected(field.getModifiers())
            ) {
                field.setAccessible(true);

                T t = mapper.map(field, generic);
                if (t != null)
                    fields.put(field.getName(), t);
            }
        }
        return fields;
    }
}
