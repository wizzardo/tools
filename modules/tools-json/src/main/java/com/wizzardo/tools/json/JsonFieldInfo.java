package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldInfo;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by wizzardo on 14/07/16.
 */
public class JsonFieldInfo extends FieldInfo<JsonFieldSetter, JsonGeneric> {
    public final Binder.Serializer serializer;

    public JsonFieldInfo(Field field, JsonFieldSetter reflection, Map<String, JsonGeneric> types, Binder.Serializer serializer) {
        super(field, reflection, types);
        this.serializer = serializer;
    }

    @Override
    protected JsonGeneric createGeneric(Field field, Map<String, JsonGeneric> types) {
        return new JsonGeneric(field.getGenericType(), types);
    }
}
