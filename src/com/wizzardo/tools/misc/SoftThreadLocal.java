package com.wizzardo.tools.misc;

import java.lang.ref.SoftReference;

/**
 * @author: wizzardo
 * Date: 8/8/14
 */
public class SoftThreadLocal<T> extends ThreadLocal<SoftReference<T>> {

    @Override
    protected SoftReference<T> initialValue() {
        return new SoftReference<T>(init());
    }

    protected T init() {
        return null;
    }

    public T getValue() {
        SoftReference<T> reference = get();
        T t = reference.get();
        if (t == null) {
            t = init();
            setValue(t);
        }
        return t;
    }

    public void setValue(T t) {
        set(new SoftReference<T>(t));
    }

}
