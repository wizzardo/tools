package com.wizzardo.tools.bytecode.fields;

public interface IntFieldSetter<T> extends FieldSetter {
    void set(T instance, int value);
}
