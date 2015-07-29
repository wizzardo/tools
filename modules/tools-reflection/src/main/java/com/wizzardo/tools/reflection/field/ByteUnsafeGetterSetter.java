package com.wizzardo.tools.reflection.field;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class ByteUnsafeGetterSetter extends AbstractGetterSetter {

    public ByteUnsafeGetterSetter(Field field) {
        super(field);
    }

    @Override
    public byte getByte(Object object) {
        return unsafe.getByte(object, offset);
    }

    @Override
    public void setByte(Object object, byte value) {
        unsafe.putByte(object, offset, value);
    }
}
