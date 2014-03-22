package com.wizzardo.tools.json;

import com.wizzardo.tools.collections.Pair;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JavaObjectBinder implements ObjectBinder {
    protected Object object;
    protected Class clazz;
    protected GenericInfo genericInfo;
    protected Map<String, Pair<Pair<Field, GenericInfo>, Binder.Serializer>> fields;

    public JavaObjectBinder(GenericInfo genericInfo) {
        this.clazz = genericInfo.clazz;
        this.genericInfo = genericInfo;
        object = Binder.createInstance(clazz);
        fields = Binder.getFields(clazz);
    }

    @Override
    public void put(String key, Object value) {
        Binder.setValue(object, fields.get(key), value);
    }

    @Override
    public void put(String key, JsonItem value) {
        put(key, value.ob);
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

        if (Map.class.isAssignableFrom(pair.key.key.getType()))
            return new JavaMapBinder(pair.key.value);

        return new JavaObjectBinder(pair.key.value);
    }

    @Override
    public ArrayBinder getArrayBinder(String key) {
        Pair<Field, GenericInfo> f = getField(key);
        if (genericInfo != null) {
            GenericInfo type = genericInfo.getGenericType(f.key);
            if (type != null)
                return new JavaArrayBinder(type);
        }

        return new JavaArrayBinder(f.value);
    }
}
