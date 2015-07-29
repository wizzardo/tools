package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class ObjectReflectionGetterSetter extends AbstractGetterSetter {

    public ObjectReflectionGetterSetter(Field field) {
        super(field);
    }

    @Override
    public void setObject(Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }

    @Override
    public Object getObject(Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }
}
