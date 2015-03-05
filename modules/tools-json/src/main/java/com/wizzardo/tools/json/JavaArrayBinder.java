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
    private Binder.SerializerType serializer;
    private Class clazz;
    private Generic generic;
    private JsonFieldSetter valueSetter;

    public JavaArrayBinder(Generic generic) {
        if (generic != null)
            this.generic = generic;
        else
            this.generic = new Generic(Object.class);

        this.clazz = this.generic.clazz;
        serializer = Binder.classToSerializer(clazz).type;
        if (serializer == Binder.SerializerType.ARRAY) {
            l = new ArrayList();
        } else if (serializer == Binder.SerializerType.COLLECTION) {
            l = Binder.createCollection(clazz);
        } else {
            throw new IllegalArgumentException("this binder only for collections and arrays! not for " + clazz);
        }

        if (generic.typeParameters.length == 1)
            valueSetter = getValueSetter(generic.typeParameters[0].clazz);
    }

    protected JsonFieldSetter getValueSetter(Class clazz) {
        final StringConverter converter = StringConverter.getConverter(clazz);
        if (converter == null)
            return null;

        return new JsonFieldSetter.ObjectSetter() {
            @Override
            public void setString(Object object, String value) {
                l.add(converter.convert(value));
            }

            @Override
            public void setObject(Object object, Object value) {
                l.add(value);
            }

            @Override
            public Type getType() {
                return converter.type;
            }
        };
    }

    @Override
    public void add(Object value) {
        l.add(value);
    }

    @Override
    public void add(JsonItem value) {
        l.add(value.getAs(generic.typeParameters[0].clazz));
    }

    @Override
    public Object getObject() {
        if (serializer == Binder.SerializerType.COLLECTION)
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

    public Generic getGeneric() {
        if (generic != null && generic.typeParameters.length != 0)
            return generic.typeParameters[0];

        return null;
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        return valueSetter;
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
        throw new UnsupportedOperationException("arrays has no keys");
    }

}
