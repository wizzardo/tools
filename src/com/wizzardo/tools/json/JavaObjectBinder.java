package com.wizzardo.tools.json;

import com.wizzardo.tools.Pair;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JavaObjectBinder implements ObjectBinder {
    private Object object;
    private Class clazz;
    private GenericInfo genericInfo;

    public JavaObjectBinder(Class clazz) {
        this(clazz, null);
    }

    public JavaObjectBinder(Class clazz, GenericInfo genericInfo) {
        this.clazz = clazz;
        this.genericInfo = genericInfo;
        object = Binder.createInstance(clazz);
    }

    @Override
    public void put(String key, Object value) {
        Binder.setValue(object, key, value);
    }

    @Override
    public void put(String key, JsonItem value) {
        Binder.setValue(object, key, value.ob);
    }

    @Override
    public Object getObject() {
        return object;
    }

    public Pair<Field, GenericInfo> getField(String key) {
        return Binder.getField(clazz, key).key;
    }

    @Override
    public ObjectBinder getObjectBinder(String key) {
        Pair<Pair<Field, GenericInfo>, Binder.Serializer> pair = Binder.getField(clazz, key);
        if (pair == null)
            return null;
        return new JavaObjectBinder(pair.key.key.getType(), pair.key.value);
    }

    @Override
    public ArrayBinder getArrayBinder(String key) {
        Pair<Field, GenericInfo> f = getField(key);
        if (genericInfo != null) {
            GenericInfo type = genericInfo.getGenericType(f.key);
            if (type != null)
                return new JavaArrayBinder(type.clazz, type.typeParameters[0]);
        }

        return new JavaArrayBinder(f.value.clazz, f.value.typeParameters.length == 1 ? f.value.typeParameters[0] : null);
    }
}
