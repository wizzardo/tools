package com.wizzardo.tools.json;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
public class JavaObjectBinder implements ObjectBinder {
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
    public void set(String key, Object value) {
        Binder.setValue(object, key, value);
    }

    @Override
    public void set(String key, JsonItem value) {
        Binder.setValue(object, key, value.ob);
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public Field getField(String key) {
        return Binder.getField(clazz, key).key;
    }
}
