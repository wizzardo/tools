package com.wizzardo.tools.reflection.field;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class DoubleUnsafeGetterSetter extends AbstractGetterSetter {

    public DoubleUnsafeGetterSetter(Field field) {
        super(field);
    }

    @Override
    public double getDouble(Object object) {
        return unsafe.getDouble(object, offset);
    }

    @Override
    public void setDouble(Object object, double value) {
        unsafe.putDouble(object, offset, value);
    }
}
