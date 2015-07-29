package com.wizzardo.tools.reflection.field;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class LongUnsafeGetterSetter extends AbstractGetterSetter {

    public LongUnsafeGetterSetter(Field field) {
        super(field);
    }

    @Override
    public long getLong(Object object) {
        return unsafe.getLong(object, offset);
    }

    @Override
    public void setLong(Object object, long value) {
        unsafe.putLong(object, offset, value);
    }
}
