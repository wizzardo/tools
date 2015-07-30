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
    final Binder.Serializer serializer;
    final JsonFieldSetter setter;

    static CharTree<String> charTree = new CharTree<String>();

    FieldInfo(Field field, Binder.Serializer serializer) {
        this.field = field;
        this.generic = new Generic(field.getGenericType());
        this.serializer = serializer;
        setter = new JsonFieldSetterFactory().create(field, true);

        if (!charTree.contains(field.getName()))
            charTree.append(field.getName(), field.getName());
    }
}
