package com.wizzardo.tools.reflection.field;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class FloatUnsafeGetterSetter extends AbstractGetterSetter {

    public FloatUnsafeGetterSetter(Field field) {
        super(field);
    }

    @Override
    public float getFloat(Object object) {
        return unsafe.getFloat(object, offset);
    }

    @Override
    public void setFloat(Object object, float value) {
        unsafe.putFloat(object, offset, value);
    }
}
