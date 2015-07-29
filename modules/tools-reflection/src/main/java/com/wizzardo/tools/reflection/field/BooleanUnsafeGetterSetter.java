package com.wizzardo.tools.reflection.field;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class BooleanUnsafeGetterSetter extends AbstractGetterSetter {

    public BooleanUnsafeGetterSetter(Field field) {
        super(field);
    }

    @Override
    public boolean getBoolean(Object object) {
        return unsafe.getBoolean(object, offset);
    }

    @Override
    public void setBoolean(Object object, boolean value) {
        unsafe.putBoolean(object, offset, value);
    }
}
