package com.wizzardo.tools.reflection.field;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class IntegerUnsafeGetterSetter extends AbstractGetterSetter {

    public IntegerUnsafeGetterSetter(Field field) {
        super(field);
    }

    @Override
    public int getInteger(Object object) {
        return unsafe.getInt(object, offset);
    }

    @Override
    public void setInteger(Object object, int value) {
        unsafe.putInt(object, offset, value);
    }
}
