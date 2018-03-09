package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.reflection.field.Type;

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
    private JsonGeneric generic;
    private JsonFieldSetter valueSetter;

    public JavaArrayBinder(JsonGeneric generic) {
        if (generic != null)
            this.generic = generic;
        else
            this.generic = generic = new JsonGeneric(Object.class);

        this.clazz = generic.clazz;
        serializer = Binder.classToSerializer(clazz).type;
        if (serializer == Binder.SerializerType.ARRAY) {
            l = new ArrayList();
        } else if (serializer == Binder.SerializerType.COLLECTION) {
            l = Binder.createCollection(clazz);
        } else {
            throw new IllegalArgumentException("JsonArray expected to parse into " + clazz + ", but JsonObject appeared");
        }

        if (generic.typesCount() == 1)
            valueSetter = getValueSetter(generic.type(0).clazz);
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
        throw new UnsupportedOperationException("Adding JsonItem is not supported while parsing into " + clazz);
    }

    @Override
    public Object getObject() {
        if (serializer == Binder.SerializerType.COLLECTION)
            return l;
        else {
            List l = (List) this.l;
            Object array = Binder.createArray(generic, l.size());
            Class type = generic.type(0).clazz;
            for (int i = 0; i < l.size(); i++) {
                Array.set(array, i, JsonItem.getAs(l.get(i), type));
            }
            return array;
        }
    }

    public JsonGeneric getGeneric() {
        if (generic.typesCount() != 0)
            return generic.type(0);

        return null;
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        return valueSetter;
    }

    @Override
    public CharTree.CharTreeNode<Pair<String, JsonFieldInfo>> getFieldsTree() {
        return null;
    }

    @Override
    public JsonBinder getObjectBinder() {
        JsonGeneric type = generic.type(0);
        if (Map.class.isAssignableFrom(type.clazz))
            return new JavaMapBinder(type);

        return new JavaObjectBinder(type);
    }

    @Override
    public JsonBinder getArrayBinder() {
        return new JavaArrayBinder(getGeneric());
    }

    @Override
    public void setTemporaryKey(String key) {
        throw new UnsupportedOperationException("JsonArray can not have any keys");
    }

    @Override
    public void setTemporaryKey(Pair<String, JsonFieldInfo> pair) {
        throw new UnsupportedOperationException("JsonArray can not have any keys");
    }

}
