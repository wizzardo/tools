package com.wizzardo.tools.json;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public class FieldInfo {
    public final Field field;
    public final Generic generic;
    public final Binder.Serializer serializer;
    public final JsonFieldSetter setter;

    public FieldInfo(Field field, Binder.Serializer serializer) {
        this.field = field;
        this.generic = new Generic(field.getGenericType());
        this.serializer = serializer;
        setter = new JsonFieldSetterFactory().create(field, true);
    }
}
