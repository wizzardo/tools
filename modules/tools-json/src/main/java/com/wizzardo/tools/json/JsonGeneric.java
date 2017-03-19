package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.Generic;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;


/**
 * Created by wizzardo on 14/07/16.
 */
public class JsonGeneric<T> extends Generic<T, JsonFields, JsonGeneric> {

    public final Binder.Serializer serializer;

    public JsonGeneric(Type c) {
        super(c);
        serializer = Binder.classToSerializer(clazz);
    }

    public JsonGeneric(Class<T> c, Class... generics) {
        super(c, generics);
        serializer = Binder.classToSerializer(clazz);
    }

    public JsonGeneric(Class<T> c, JsonGeneric... generics) {
        super(c, generics);
        serializer = Binder.classToSerializer(clazz);
    }

    protected JsonGeneric(Type c, Map<String, JsonGeneric> types) {
        super(c, types);
        serializer = Binder.classToSerializer(clazz);
    }

    public JsonGeneric(Type c, Map<String, JsonGeneric> types, Map<Type, Generic<T, JsonFields, JsonGeneric>> cyclicDependencies) {
        super(c, types, cyclicDependencies);
        serializer = Binder.classToSerializer(clazz);
    }

    @Override
    public JsonFields getFields() {
        if (fields != null)
            return fields;

        fields = Binder.getFields(clazz);
        return fields;
    }

    @Override
    public JsonGeneric type(int i) {
        return super.type(i);
    }

    @Override
    public JsonGeneric getGenericType(Field f) {
        return super.getGenericType(f);
    }

    @Override
    public JsonGeneric parent() {
        return super.parent();
    }

    @Override
    protected JsonGeneric[] createArray(int size) {
        return new JsonGeneric[size];
    }

    @Override
    protected JsonGeneric create(Type c) {
        return new JsonGeneric(c);
    }

    @Override
    protected JsonGeneric create(Class<T> c, Class... generics) {
        return new JsonGeneric(c, generics);
    }

    @Override
    protected JsonGeneric create(Type c, Map<String, JsonGeneric> types) {
        return new JsonGeneric(c, types);
    }

    @Override
    protected JsonGeneric create(Type c, Map<String, JsonGeneric> types, Map<Type, Generic<T, JsonFields, JsonGeneric>> cyclicDependencies) {
        return new JsonGeneric(c, types, cyclicDependencies);
    }
}
