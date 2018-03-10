package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.reflection.Generic;

import java.lang.reflect.*;
import java.util.Map;


/**
 * Created by wizzardo on 14/07/16.
 */
public class JsonGeneric<T> extends Generic<T, JsonFields, JsonGeneric> {

    public final Binder.Serializer serializer;
    protected CharTree<Pair<String, JsonFieldInfo>> fieldsTree;

    public static <T, G extends JsonGeneric> JsonGeneric<T> copyWithoutTypesAndInterfaces(JsonGeneric<T> generic, G... generics) {
        return new JsonGeneric<T>(generic.clazz, generic.parent, generics, generic.serializer);
    }

    protected JsonGeneric(Class c, JsonGeneric parent, JsonGeneric[] typeParameters, Binder.Serializer serializer) {
        super(c, parent, typeParameters);
        this.serializer = serializer;
    }

    public JsonGeneric(Type c) {
        super(c);
        serializer = createSerializer(c);
    }

    public JsonGeneric(Class<T> c, Class... generics) {
        super(c, generics);
        serializer = createSerializer(c);
    }

    public JsonGeneric(Class<T> c, JsonGeneric... generics) {
        super(c, generics);
        serializer = createSerializer(c);
    }

    protected JsonGeneric(Type c, Map<String, JsonGeneric> types) {
        super(c, types);
        serializer = createSerializer(c);
    }

    public JsonGeneric(Type c, Map<String, JsonGeneric> types, Map<Type, Generic<T, JsonFields, JsonGeneric>> cyclicDependencies) {
        super(c, types, cyclicDependencies);
        serializer = createSerializer(c);
    }

    protected Binder.Serializer createSerializer(Type c) {
        return Binder.classToSerializer(clazz == Array.class && !(c instanceof GenericArrayType) ? (Class) c : clazz);
    }

    @Override
    public JsonFields getFields() {
        if (fields != null)
            return fields;

        fields = Binder.getFields(this);
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

    public CharTree<Pair<String, JsonFieldInfo>> getFieldsTree() {
        if (fieldsTree != null)
            return fieldsTree;

        fieldsTree = new CharTree<Pair<String, JsonFieldInfo>>();
        for (JsonFieldInfo info : fields.fields()) {
            fieldsTree.append(info.field.getName(), Pair.of(info.field.getName(), info));
        }
        return fieldsTree;
    }
}
