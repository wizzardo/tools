package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.FieldReflectionFactory;
import com.wizzardo.tools.reflection.Generic;

import java.lang.reflect.*;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by wizzardo on 14/07/16.
 */
public class JsonGeneric<T> extends Generic<T, JsonFields, JsonGeneric> {

    public final Binder.Serializer serializer;
    protected CharTree<Pair<String, JsonFieldInfo>> fieldsTree;
    public final SerializationContext context;

    static ReentrantLock initLock = new ReentrantLock();
    static SerializationContext initContext;
    static FieldReflection contextField;

    static {
        try {
            contextField = new FieldReflectionFactory().create(JsonGeneric.class, "context", true);
        } catch (NoSuchFieldException ignored) {
        }
    }

    public static <T, G extends JsonGeneric> JsonGeneric<T> copyWithoutTypesAndInterfaces(JsonGeneric<T> generic, G... generics) {
        return new JsonGeneric<T>(generic.context, generic.clazz, generic.parent, generics, generic.serializer);
    }

    protected JsonGeneric(SerializationContext context, Class c, JsonGeneric parent, JsonGeneric[] typeParameters, Binder.Serializer serializer) {
        super(initStart(c, context), parent, typeParameters);
        this.context = context;
        this.serializer = serializer;
    }

    public JsonGeneric(Type c) {
        this(Binder.DEFAULT_SERIALIZATION_CONTEXT, c);
    }

    public JsonGeneric(Class<T> c, Class... generics) {
        this(Binder.DEFAULT_SERIALIZATION_CONTEXT, c, generics);
    }

    public JsonGeneric(Class<T> c, JsonGeneric... generics) {
        this(Binder.DEFAULT_SERIALIZATION_CONTEXT, c, generics);
    }

    protected JsonGeneric(Type c, Map<String, JsonGeneric> types) {
        this(Binder.DEFAULT_SERIALIZATION_CONTEXT, c, types);
    }

    public JsonGeneric(SerializationContext context, Class<T> c) {
        super(initStart(c, context));
        this.context = context;
        serializer = createSerializer(c);
    }

    public JsonGeneric(SerializationContext context, Type c) {
        super(initStart(c, context));
        this.context = context;
        serializer = createSerializer(c);
    }

    public JsonGeneric(SerializationContext context, Class<T> c, Class... generics) {
        super(initStart(c, context), generics);
        this.context = context;
        serializer = createSerializer(c);
    }

    public JsonGeneric(SerializationContext context, Class<T> c, JsonGeneric... generics) {
        super(initStart(c, context), generics);
        this.context = context;
        serializer = createSerializer(c);
    }

    protected JsonGeneric(SerializationContext context, Type c, Map<String, JsonGeneric> types) {
        super(initStart(c, context), types);
        this.context = context;
        serializer = createSerializer(c);
    }

    public JsonGeneric(SerializationContext context, Type c, Map<String, JsonGeneric> types, Map<Type, Generic<T, JsonFields, JsonGeneric>> cyclicDependencies) {
        super(initStart(c, context), types, cyclicDependencies);
        this.context = context;
        serializer = createSerializer(c);
    }

    @Override
    protected void init() {
        contextField.setObject(this, initContext);
        initEnd();
    }

    protected static Type initStart(Type t, SerializationContext context) {
        initLock.lock();
        initContext = context;
        return t;
    }

    protected static <T> Class<T> initStart(Class<T> t, SerializationContext context) {
        initLock.lock();
        initContext = context;
        return t;
    }

    protected static void initEnd() {
        initLock.unlock();
    }

    protected Binder.Serializer createSerializer(Type c) {
        return Binder.classToSerializer(clazz == Array.class && !(c instanceof GenericArrayType) ? (Class) c : clazz);
    }

    @Override
    public JsonFields getFields() {
        if (fields != null)
            return fields;

        fields = context.getFields(this);
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
        return new JsonGeneric(context, c);
    }

    @Override
    protected JsonGeneric create(Class<T> c, Class... generics) {
        return new JsonGeneric(context, c, generics);
    }

    @Override
    protected JsonGeneric create(Type c, Map<String, JsonGeneric> types) {
        return new JsonGeneric(context, c, types);
    }

    @Override
    protected JsonGeneric create(Type c, Map<String, JsonGeneric> types, Map<Type, Generic<T, JsonFields, JsonGeneric>> cyclicDependencies) {
        return new JsonGeneric(context, c, types, cyclicDependencies);
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
