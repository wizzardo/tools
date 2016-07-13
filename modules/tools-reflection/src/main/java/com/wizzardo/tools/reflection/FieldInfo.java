package com.wizzardo.tools.reflection;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public class FieldInfo<T extends FieldReflection, G extends Generic> {
    public final Field field;
    public final G generic;
    public final T reflection;

    public FieldInfo(Field field, T reflection) {
        this.field = field;
        this.reflection = reflection;
        this.generic = createGeneric(field);
    }

    protected G createGeneric(Field field) {
        return (G) new Generic(field.getGenericType());
    }
}
