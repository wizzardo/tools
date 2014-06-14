package com.wizzardo.tools.json;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
class FieldInfo {
    final Field field;
    final Generic generic;
    final Binder.Serializer serializer;
    final JsonFieldSetter setter;

    FieldInfo(Field field, Binder.Serializer serializer) {
        this.field = field;
        this.generic = new Generic(field.getGenericType());
        this.serializer = serializer;
        setter = JsonFieldSetter.createSetter(field);
    }
}
