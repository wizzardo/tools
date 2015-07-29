package com.wizzardo.tools.reflection.field;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class CharUnsafeGetterSetter extends AbstractGetterSetter {

    public CharUnsafeGetterSetter(Field field) {
        super(field);
    }

    @Override
    public char getChar(Object object) {
        return unsafe.getChar(object, offset);
    }

    @Override
    public void setChar(Object object, char value) {
        unsafe.putChar(object, offset, value);
    }
}
