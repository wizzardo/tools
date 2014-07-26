package com.wizzardo.tools.json;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JavaArrayBinder implements JsonBinder {
    private Collection l;
    private Binder.Serializer serializer;
    private Class clazz;
    private Generic generic;

    public JavaArrayBinder(Generic generic) {
        if (generic != null)
            this.generic = generic;
        else
            this.generic = new Generic(Object.class);

        this.clazz = generic.clazz;
        serializer = Binder.classToSerializer(clazz);
        if (serializer == Binder.Serializer.ARRAY) {
            l = new ArrayList();
        } else if (serializer == Binder.Serializer.COLLECTION) {
            l = Binder.createCollection(clazz);
        } else {
            throw new IllegalArgumentException("this binder only for collections and arrays! not for " + clazz);
        }

    }

    @Override
    public void add(Object value) {
        add(new JsonItem(value));
    }

    @Override
    public void add(JsonItem value) {
        l.add(value.getAs(generic.typeParameters[0].clazz));
    }

    @Override
    public Object getObject() {
        if (serializer == Binder.Serializer.COLLECTION)
            return l;
        else {
            List l = (List) this.l;
            Object array = Binder.createArray(generic, l.size());
            Class type = generic.typeParameters[0].clazz;
            for (int i = 0; i < l.size(); i++) {
                Array.set(array, i, JsonItem.getAs(l.get(i), type));
            }
            return array;
        }
    }

    @Override
    public Generic getGeneric() {
        if (generic != null && generic.typeParameters.length != 0)
            return generic.typeParameters[0];

        return null;
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        return null;
    }

    @Override
    public JsonBinder getObjectBinder() {
        if (Map.class.isAssignableFrom(generic.typeParameters[0].clazz))
            return new JavaMapBinder(generic.typeParameters[0]);

        return new JavaObjectBinder(generic.typeParameters[0]);
    }

    @Override
    public JsonBinder getArrayBinder() {
        return new JavaArrayBinder(getGeneric());
    }

    @Override
    public void setTemporaryKey(String key) {
    }
}
