package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class BooleanReflectionGetterSetter extends AbstractGetterSetter {

    public BooleanReflectionGetterSetter(Field field) {
        super(field);
    }

    @Override
    public void setBoolean(Object object, boolean value) {
        try {
            field.setBoolean(object, value);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }

    @Override
    public boolean getBoolean(Object object) {
        try {
            return field.getBoolean(object);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }
}
