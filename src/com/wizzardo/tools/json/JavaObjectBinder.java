package com.wizzardo.tools.json;

import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JavaObjectBinder implements JsonBinder {
    protected Object object;
    protected Class clazz;
    protected Generic generic;
    protected Map<String, FieldInfo> fields;
    protected String tempKey;

    public JavaObjectBinder(Generic generic) {
        this.clazz = generic.clazz;
        this.generic = generic;
        object = Binder.createInstance(clazz);
        fields = Binder.getFields(clazz);
    }

    @Override
    public void add(Object value) {
        add(new JsonItem(value));
    }

    @Override
    public void add(JsonItem value) {
        Binder.setValue(object, fields.get(tempKey), value);
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public JsonBinder getObjectBinder() {
        FieldInfo info = fields.get(tempKey);
        if (info == null)
            return null;

        if (Map.class.isAssignableFrom(info.field.getType()))
            return new JavaMapBinder(info.generic);

        return new JavaObjectBinder(info.generic);
    }

    @Override
    public JsonBinder getArrayBinder() {
        FieldInfo info = fields.get(tempKey);
        if (generic != null) {
            Generic type = generic.getGenericType(info.field);
            if (type != null)
                return new JavaArrayBinder(type);
        }

        return new JavaArrayBinder(info.generic);
    }

    @Override
    public void setTemporaryKey(String key) {
        tempKey = key;
    }

    @Override
    public Generic getGeneric() {
        return null;
    }
}
