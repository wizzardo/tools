package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldInfo;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 14/07/16.
 */
public class JsonFieldInfo extends FieldInfo<JsonFieldSetter, JsonGeneric> {
    public final Binder.Serializer serializer;

    public JsonFieldInfo(Field field, JsonFieldSetter reflection, Binder.Serializer serializer) {
        super(field, reflection);
        this.serializer = serializer;
    }

    @Override
    protected JsonGeneric createGeneric(Field field) {
        return new JsonGeneric(field.getGenericType());
    }
}
