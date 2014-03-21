package com.wizzardo.tools.json;

import java.util.Map;

/**
 * @author: wizzardo
 * Date: 3/14/14
 */
public class JavaMapBinder extends JavaObjectBinder {
    private Map that;
    private GenericInfo[] type;

    public JavaMapBinder(GenericInfo genericInfo) {
        super(genericInfo);
        that = (Map) object;
        if (genericInfo.typeParameters.length != 2)
            type = new GenericInfo[]{new GenericInfo(Object.class), new GenericInfo(Object.class)};
        else
            type = genericInfo.typeParameters;

    }

    @Override
    public void put(String key, Object value) {
        if (Binder.setValue(object, key, value))
            return;

        that.put(JsonItem.getAs(key, type[0].clazz), value);
    }

    @Override
    public void put(String key, JsonItem value) {
        if (Binder.setValue(object, key, value))
            return;

        that.put(JsonItem.getAs(key, type[0].clazz), value.getAs(type[1].clazz));
    }

    @Override
    public ObjectBinder getObjectBinder(String key) {
        if (genericInfo.typeParameters.length == 2)
            return new JavaObjectBinder(genericInfo.typeParameters[1]);

        return super.getObjectBinder(key);
    }

    @Override
    public ArrayBinder getArrayBinder(String key) {
        if (genericInfo.typeParameters.length == 2)
            return new JavaArrayBinder(genericInfo.typeParameters[1]);

        return super.getArrayBinder(key);
    }
}