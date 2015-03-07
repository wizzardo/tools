package com.wizzardo.tools.json;

import java.util.Map;

/**
 * @author: wizzardo
 * Date: 3/14/14
 */
public class JavaMapBinder extends JavaObjectBinder {
    private Map that;
    private Generic[] type;
    private boolean valueIsMap;
    private JsonFieldSetter valueSetter;
    private StringConverter keyConverter;

    public JavaMapBinder(Generic generic) {
        super(generic);
        that = (Map) object;
        type = getTypes(generic);
        valueIsMap = Map.class.isAssignableFrom(type[1].clazz);
        keyConverter = StringConverter.getConverter(type[0].clazz);
        valueSetter = getValueSetter(type[1].clazz);
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
        that.put(keyConverter.convert(tempKey), value);
    }

    @Override
    protected Map createInstance(Class clazz) {
        return Binder.createMap(clazz);
    }

    private Generic[] getTypes(Generic generic) {
        if (generic == null)
            return new Generic[]{new Generic(Object.class), new Generic(Object.class)};

        if (generic.typeParameters.length != 2)
            return getTypes(generic.parent);
        return generic.typeParameters;
    }

    @Override
    public void add(Object value) {
        put(keyConverter, value);
    }

    @Override
    public void add(JsonItem value) {
        if (Binder.setValue(object, tempKey, value))
            return;

        that.put(JsonItem.getAs(tempKey, type[0].clazz), value.getAs(type[1].clazz));
    }

    @Override
    public JsonBinder getObjectBinder() {
        JsonBinder binder = super.getObjectBinder();
        if (binder != null)
            return binder;

        if (valueIsMap)
            return new JavaMapBinder(type[1]);
        else
            return new JavaObjectBinder(type[1]);
    }

    @Override
    public JsonBinder getArrayBinder() {
        JsonBinder binder = super.getArrayBinder();
        if (binder != null)
            return binder;

        return new JavaArrayBinder(type[1]);
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        return valueSetter;
    }
}