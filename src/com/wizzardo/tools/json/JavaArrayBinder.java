package com.wizzardo.tools.json;

import com.wizzardo.tools.collections.Pair;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JavaArrayBinder implements ArrayBinder {
    private Collection l;
    private Binder.Serializer serializer;
    private Class clazz;
    private GenericInfo generic;

    public JavaArrayBinder(Class clazz, GenericInfo generic) {
        if (generic != null)
            this.generic = generic;
        else
            this.generic = new GenericInfo(Object.class);

        this.clazz = clazz;
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
        l.add(value);
    }

    @Override
    public void add(JsonItem value) {
        l.add(value.getAs(generic.clazz));
    }

    @Override
    public Object getObject() {
        if (serializer == Binder.Serializer.COLLECTION)
            return l;
        else {
            List l = (List) this.l;
            Object array = Binder.createArray(clazz, generic, l.size());
            Class type = Binder.getArrayType(clazz, generic);
            for (int i = 0; i < l.size(); i++) {
                Array.set(array, i, JsonItem.getAs(l.get(i), type));
            }
            return array;
        }
    }

    @Override
    public Pair<Class, Type> getGeneric() {
        Type type = null;
        if (generic != null && generic instanceof ParameterizedType)
            type = ((ParameterizedType) generic).getActualTypeArguments()[0];

        Class cl = null;
        Type inner = null;
        if (type != null)
            if (type instanceof ParameterizedType) {
                cl = (Class) ((ParameterizedType) type).getRawType();
                inner = type;
            } else
                cl = (Class) type;

        return new Pair<Class, Type>(cl, inner);
    }

    @Override
    public ObjectBinder getObjectBinder() {
        if (Map.class.isAssignableFrom(generic.clazz))
            return new JavaMapBinder(generic.clazz, generic);

        return new JavaObjectBinder(generic.clazz, generic);
    }

    @Override
    public ArrayBinder getArrayBinder() {
        Pair<Class, Type> pair = getGeneric();
        return new JavaArrayBinder(pair.key, null);
    }
}
