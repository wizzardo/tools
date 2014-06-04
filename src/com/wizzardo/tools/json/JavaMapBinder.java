package com.wizzardo.tools.json;

import java.util.Map;

/**
 * @author: wizzardo
 * Date: 3/14/14
 */
public class JavaMapBinder extends JavaObjectBinder {
    private Map that;
    private Generic[] type;

    public JavaMapBinder(Generic generic) {
        super(generic);
        that = (Map) object;
        if (generic.typeParameters.length != 2)
            type = new Generic[]{new Generic(Object.class), new Generic(Object.class)};
        else
            type = generic.typeParameters;

    }

    @Override
    public void put(String key, Object value) {
        put(key, new JsonItem(value));
    }

    @Override
    public void put(String key, JsonItem value) {
        if (Binder.setValue(object, key, value))
            return;

        that.put(JsonItem.getAs(key, type[0].clazz), value.getAs(type[1].clazz));
    }

    @Override
    public ObjectBinder getObjectBinder(String key) {
        if (generic.typeParameters.length == 2)
            return new JavaObjectBinder(generic.typeParameters[1]);

        return super.getObjectBinder(key);
    }

    @Override
    public ArrayBinder getArrayBinder(String key) {
        if (generic.typeParameters.length == 2)
            return new JavaArrayBinder(generic.typeParameters[1]);

        return super.getArrayBinder(key);
    }
}