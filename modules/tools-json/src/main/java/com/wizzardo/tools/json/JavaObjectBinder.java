package com.wizzardo.tools.json;

import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JavaObjectBinder implements JsonBinder {
    protected Object object;
    protected Class clazz;
    protected JsonGeneric generic;
    protected JsonFields fields;
    protected String tempKey;

    public JavaObjectBinder(JsonGeneric generic) {
        this.clazz = generic.clazz;
        this.generic = generic;
        object = createInstance(clazz);
        fields = generic.getFields();
    }

    protected Object createInstance(Class clazz) {
        return Binder.createObject(clazz);
    }

    private JsonFieldInfo getField() {
        return fields.get(tempKey);
    }

    @Override
    public void add(Object value) {
        JsonFieldInfo fieldInfo = getField();
        if (fieldInfo == null)
            return;
        fieldInfo.reflection.setObject(object, value);
    }

    @Override
    public void add(JsonItem value) {
        throw new UnsupportedOperationException("Adding JsonItem is not supported while parsing into " + clazz);
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public JsonBinder getObjectBinder() {
        JsonFieldInfo info = getField();
        if (info == null)
            return null;

        if (Map.class.isAssignableFrom(info.field.getType()))
            return new JavaMapBinder(info.generic);

        return new JavaObjectBinder(info.generic);
    }

    @Override
    public JsonBinder getArrayBinder() {
        JsonFieldInfo info = getField();
        if (info == null)
            return null;

        JsonGeneric type = generic.getGenericType(info.field);
        if (type != null)
            return new JavaArrayBinder(type);

        return new JavaArrayBinder(info.generic);
    }

    @Override
    public void setTemporaryKey(String key) {
        tempKey = key;
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        JsonFieldInfo f = getField();
        if (f != null)
            return f.reflection;
        return null;
    }
}
