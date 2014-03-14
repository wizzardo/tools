package com.wizzardo.tools.json;

import java.util.Map;

/**
 * @author: wizzardo
 * Date: 3/14/14
 */
public class JavaMapBinder extends JavaObjectBinder {
    private boolean wrap = false;
    private Map that;

    public JavaMapBinder(Class clazz) {
        super(clazz);
    }

    public JavaMapBinder(Class clazz, GenericInfo genericInfo) {
        super(clazz, genericInfo);
        that = (Map) object;
        wrap = genericInfo.typeParameters.length == 2 && genericInfo.typeParameters[0].clazz != Object.class && genericInfo.typeParameters[0].clazz != String.class;
    }

    @Override
    public void put(String key, Object value) {
        if (Binder.setValue(object, key, value))
            return;

        if (wrap)
            that.put(JsonItem.getAs(key, genericInfo.typeParameters[0].clazz), value);
        else
            that.put(key, value);
    }

    @Override
    public ObjectBinder getObjectBinder(String key) {
        if (genericInfo.typeParameters.length == 2)
            return new JavaObjectBinder(genericInfo.typeParameters[1].clazz, genericInfo.typeParameters[1]);

        return super.getObjectBinder(key);
    }
    @Override
    public ArrayBinder getArrayBinder(String key) {
        if (genericInfo.typeParameters.length == 2)
            return new JavaArrayBinder(genericInfo.typeParameters[1].clazz, genericInfo.typeParameters[1]);

        return super.getArrayBinder(key);
    }
}