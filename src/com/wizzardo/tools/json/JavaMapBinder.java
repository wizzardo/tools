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
    public void add(Object value) {
        add(new JsonItem(value));
    }

    @Override
    public void add(JsonItem value) {
        if (Binder.setValue(object, tempKey, value))
            return;

        that.put(JsonItem.getAs(tempKey, type[0].clazz), value.getAs(type[1].clazz));
    }

    @Override
    public JsonBinder getObjectBinder() {
        if (generic.typeParameters.length == 2)
            return new JavaObjectBinder(generic.typeParameters[1]);

        return super.getObjectBinder();
    }

    @Override
    public JsonBinder getArrayBinder() {
        if (generic.typeParameters.length == 2)
            return new JavaArrayBinder(generic.typeParameters[1]);

        return super.getArrayBinder();
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        return null;
    }
}