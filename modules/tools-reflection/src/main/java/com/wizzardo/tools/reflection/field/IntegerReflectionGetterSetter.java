package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class IntegerReflectionGetterSetter extends AbstractGetterSetter {

    public IntegerReflectionGetterSetter(Field field) {
        super(field);
    }

    @Override
    public void setInteger(Object object, int value) {
        try {
            field.setInt(object, value);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }

    @Override
    public int getInteger(Object object) {
        try {
            return field.getInt(object);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }
}
