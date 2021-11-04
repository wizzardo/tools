package com.wizzardo.tools.bytecode.fields;

public interface LongFieldSetter<T> extends FieldSetter {
    void set(T instance, long value);
}
