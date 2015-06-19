package com.wizzardo.tools.misc.pool;

import java.lang.ref.SoftReference;

/**
 * Created by wizzardo on 19.06.15.
 */
public class SoftHolder<T> implements Holder<T> {
    protected final Pool<T> pool;
    protected volatile SoftReference<T> ref;

    public SoftHolder(Pool<T> pool, T value) {
        this.pool = pool;
        ref = new SoftReference<T>(value);
    }

    @Override
    public T get() {
        T value = ref.get();
        if (value == null) {
            value = pool.create();
            ref = new SoftReference<T>(value);
        }
        return value;
    }

    @Override
    public void close() {
        pool.release(this);
    }
}
