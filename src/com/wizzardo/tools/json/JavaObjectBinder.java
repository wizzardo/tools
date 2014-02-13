package com.wizzardo.tools.json;

import com.wizzardo.tools.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JavaObjectBinder implements ObjectBinder {
    private Object object;
    private Class clazz;

    public JavaObjectBinder(Class clazz) {
        this(clazz, null);
    }

    public JavaObjectBinder(Class clazz, Type generic) {
        this.clazz = clazz;
        object = Binder.createInstance(clazz, generic);
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

    public Field getField(String key) {
        return Binder.getField(clazz, key).key;
    }

    @Override
    public ObjectBinder getObjectBinder(String key) {
        Pair<Field, Binder.Serializer> pair = Binder.getField(clazz, key);
        if (pair == null)
            return null;
        return new JavaObjectBinder(pair.key.getType());
    }

    @Override
    public ArrayBinder getArrayBinder(String key) {
        Field f = getField(key);
        return new JavaArrayBinder(f.getType(), f.getGenericType());
    }
}
