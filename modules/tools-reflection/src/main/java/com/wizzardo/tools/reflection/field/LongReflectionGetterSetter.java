package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class LongReflectionGetterSetter extends AbstractGetterSetter {

    public LongReflectionGetterSetter(Field field) {
        super(field);
    }

    @Override
    public void setLong(Object object, long value) {
        try {
            field.setLong(object, value);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }

    @Override
    public long getLong(Object object) {
        try {
            return field.getLong(object);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }
}
