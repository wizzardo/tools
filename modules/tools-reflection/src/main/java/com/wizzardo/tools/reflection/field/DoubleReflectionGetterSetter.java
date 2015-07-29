package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class DoubleReflectionGetterSetter extends AbstractGetterSetter {

    public DoubleReflectionGetterSetter(Field field) {
        super(field);
    }

    @Override
    public void setDouble(Object object, double value) {
        try {
            field.setDouble(object, value);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }

    @Override
    public double getDouble(Object object) {
        try {
            return field.getDouble(object);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }
}
