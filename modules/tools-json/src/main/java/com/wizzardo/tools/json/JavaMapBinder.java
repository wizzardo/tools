package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.StringConverter;
import com.wizzardo.tools.reflection.field.Type;

import java.util.Map;

/**
 * @author: wizzardo
 * Date: 3/14/14
 */
public class JavaMapBinder extends JavaObjectBinder {
    private Map that;
    private JsonGeneric type;
    private boolean valueIsMap;
    private JsonFieldSetter valueSetter;
    private StringConverter keyConverter;

    public JavaMapBinder(JsonGeneric generic) {
        super(generic);
        that = (Map) object;
        JsonGeneric types = getTypes(generic);
        type = types.type(1);
        keyConverter = StringConverter.getConverter(types.type(0).clazz);
        valueIsMap = Map.class.isAssignableFrom(type.clazz);
        valueSetter = getValueSetter(type.clazz);
    }

    protected JsonFieldSetter getValueSetter(Class classValue) {
        final StringConverter valueConverter = StringConverter.getConverter(classValue);
        if (valueConverter == null || keyConverter == null)
            return null;

        return new JsonFieldSetter.ObjectSetter() {
            @Override
            public void setString(Object object, String value) {
                put(keyConverter, valueConverter, value);
            }

            @Override
            public void setObject(Object object, Object value) {
                put(keyConverter, value);
            }

            @Override
            public Type getType() {
                return valueConverter.type;
            }
        };
    }

    protected void put(StringConverter keyConverter, StringConverter valueConverter, String value) {
        put(keyConverter, valueConverter.convert(value));
    }

    protected void put(StringConverter keyConverter, Object value) {
        that.put(keyConverter.convert(tempKeyName), value);
    }

    @Override
    public void setTemporaryKey(String key) {
        tempKeyName = key;
    }

    @Override
    protected Map createInstance(Class clazz) {
        return Binder.createMap(clazz);
    }

    private JsonGeneric getTypes(JsonGeneric generic) {
        if (generic == null)
            return new JsonGeneric(Object.class, Object.class, Object.class);

        if (generic.typesCount() != 2)
            return getTypes(generic.parent());
        return generic;
    }

    @Override
    public void add(Object value) {
        put(keyConverter, value);
    }

    @Override
    public JsonBinder getObjectBinder() {
        if (valueIsMap)
            return new JavaMapBinder(type);
        else
            return new JavaObjectBinder(type);
    }

    @Override
    public JsonBinder getArrayBinder() {
        return new JavaArrayBinder(type);
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        return valueSetter;
    }
}