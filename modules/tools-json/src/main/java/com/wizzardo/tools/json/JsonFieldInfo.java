package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldInfo;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by wizzardo on 14/07/16.
 */
public class JsonFieldInfo extends FieldInfo<JsonFieldSetter, JsonGeneric> {
    public final Binder.Serializer serializer;
    protected String preparedFieldName;

    public JsonFieldInfo(Field field, JsonFieldSetter reflection, JsonGeneric generic, Binder.Serializer serializer) {
        super(field, reflection, generic);
        this.serializer = serializer;
    }

    protected void prepareName(boolean firstField) {
        String fieldName = field.getName();

        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        if (annotation != null)
            fieldName = annotation.value();

        if (firstField)
            preparedFieldName = "{\"" + fieldName + "\":";
        else
            preparedFieldName = ",\"" + fieldName + "\":";
    }

    public String getPreparedFieldName() {
        return preparedFieldName;
    }

    @Override
    protected JsonGeneric createGeneric(Field field, Map<String, JsonGeneric> types) {
        return new JsonGeneric(field.getGenericType(), types);
    }
}
