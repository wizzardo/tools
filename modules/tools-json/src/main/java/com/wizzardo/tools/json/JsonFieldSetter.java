package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.field.*;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 3/22/14
 */
public interface JsonFieldSetter extends FieldReflection {

    void setString(Object object, String value);

    public static class ObjectSetter extends AbstractGetterSetter implements JsonFieldSetter {
        protected ObjectSetter() {
        }

        ObjectSetter(Field f) {
            super(f);
        }

        @Override
        public void setInteger(Object object, int value) {
            setObject(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setObject(object, value);
        }

        @Override
        public void setByte(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setObject(object, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setObject(object, value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setObject(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setObject(object, value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setObject(object, value);
        }

        @Override
        public void setString(Object object, String value) {
            setObject(object, value);
        }
    }
}
