package com.wizzardo.tools.json;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
class FieldInfo {
    final Field field;
    final GenericInfo genericInfo;
    final Binder.Serializer serializer;
    final FieldSetter setter;

    FieldInfo(Field field, Binder.Serializer serializer) {
        this.field = field;
        this.genericInfo = new GenericInfo(field.getGenericType());
        this.serializer = serializer;
        setter = FieldSetter.getSetter(field.getType());
    }
}
