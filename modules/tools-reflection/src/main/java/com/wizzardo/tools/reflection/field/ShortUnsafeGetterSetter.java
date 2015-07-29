package com.wizzardo.tools.reflection.field;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class ShortUnsafeGetterSetter extends AbstractGetterSetter {

    public ShortUnsafeGetterSetter(Field field) {
        super(field);
    }

    @Override
    public short getShort(Object object) {
        return unsafe.getShort(object, offset);
    }

    @Override
    public void setShort(Object object, short value) {
        unsafe.putShort(object, offset, value);
    }
}
