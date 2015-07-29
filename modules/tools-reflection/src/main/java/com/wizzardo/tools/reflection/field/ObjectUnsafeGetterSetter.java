package com.wizzardo.tools.reflection.field;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class ObjectUnsafeGetterSetter extends AbstractGetterSetter {

    public ObjectUnsafeGetterSetter(Field field) {
        super(field);
    }

    @Override
    public Object getObject(Object object) {
        return unsafe.getObject(object, offset);
    }

    @Override
    public void setObject(Object object, Object value) {
        unsafe.putObject(object, offset, value);
    }
}
