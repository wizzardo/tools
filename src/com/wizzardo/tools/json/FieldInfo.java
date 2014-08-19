package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
class FieldInfo {
    final Field field;
    final Generic generic;
    final Binder.SerializerType serializer;
    final JsonFieldSetter setter;

    static CharTree charTree = new CharTree();

    FieldInfo(Field field, Binder.SerializerType serializer) {
        this.field = field;
        this.generic = new Generic(field.getGenericType());
        this.serializer = serializer;
        setter = JsonFieldSetter.createSetter(field);

        if (!charTree.contains(field.getName()))
            charTree.append(field.getName());
    }
}
