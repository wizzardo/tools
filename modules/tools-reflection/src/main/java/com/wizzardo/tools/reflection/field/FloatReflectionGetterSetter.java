package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class FloatReflectionGetterSetter extends AbstractGetterSetter {

    public FloatReflectionGetterSetter(Field field) {
        super(field);
    }

    @Override
    public void setFloat(Object object, float value) {
        try {
            field.setFloat(object, value);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }

    @Override
    public float getFloat(Object object) {
        try {
            return field.getFloat(object);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }
}
