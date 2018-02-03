package com.wizzardo.tools.reflection;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public class FieldInfo<T extends FieldReflection, G extends Generic> {
    public final Field field;
    public final G generic;
    public final T reflection;

    public FieldInfo(Field field, T reflection, G generic) {
        this.field = field;
        this.reflection = reflection;
        this.generic = generic;
    }

    public FieldInfo(Field field, T reflection) {
        this.field = field;
        this.reflection = reflection;
        this.generic = createGeneric(field, Collections.<String, G>emptyMap());
    }

    public FieldInfo(Field field, T reflection, Map<String, G> types) {
        this.field = field;
        this.reflection = reflection;
        this.generic = createGeneric(field, types);
    }

    protected G createGeneric(Field field, Map<String, G> types) {
        return (G) new Generic(field.getGenericType(), types);
    }
}
