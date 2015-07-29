package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class CharReflectionGetterSetter extends AbstractGetterSetter {

    public CharReflectionGetterSetter(Field field) {
        super(field);
    }

    @Override
    public void setChar(Object object, char value) {
        try {
            field.setChar(object, value);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }

    @Override
    public char getChar(Object object) {
        try {
            return field.getChar(object);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }
}
