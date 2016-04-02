package com.wizzardo.tools.misc;

import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;

/**
 * @author: wizzardo
 * Date: 8/8/14
 */
public class SoftThreadLocal<T> extends ThreadLocal<SoftReference<T>> {

    protected final Callable<T> supplier;

    public SoftThreadLocal(Callable<T> supplier) {
        this.supplier = supplier;
    }

    protected T init() {
        return Unchecked.call(supplier);
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

    public final void setValue(T t) {
        set(new SoftReference<T>(t));
    }

    @Override
    protected final SoftReference<T> initialValue() {
        return new SoftReference<T>(init());
    }
}
