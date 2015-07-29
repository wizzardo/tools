package com.wizzardo.tools.reflection.field;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Field;

/**
 * Created by wizzardo on 28.07.15.
 */
public class ByteReflectionGetterSetter extends AbstractGetterSetter {

    public ByteReflectionGetterSetter(Field field) {
        super(field);
    }

    @Override
    public void setByte(Object object, byte value) {
        try {
            field.setByte(object, value);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }

    @Override
    public byte getByte(Object object) {
        try {
            return field.getByte(object);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
    }
}
