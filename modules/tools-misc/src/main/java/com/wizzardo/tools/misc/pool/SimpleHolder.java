package com.wizzardo.tools.misc.pool;

/**
 * Created by wizzardo on 12/08/16.
 */
public class SimpleHolder<T> implements Holder<T> {
    protected final Pool<T> pool;
    protected final T value;

    public SimpleHolder(Pool<T> pool, T value) {
        this.pool = pool;
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void close() {
        pool.release(this);
    }
}
