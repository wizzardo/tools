package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class ShortReflectionGetterSetter extends AbstractGetterSetter {

    public ShortReflectionGetterSetter(Field field) {
        super(field);
    }

    @Override
    public void setShort(Object object, short value) {
        try {
            field.setShort(object, value);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }

    @Override
    public short getShort(Object object) {
        try {
            return field.getShort(object);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }
}
