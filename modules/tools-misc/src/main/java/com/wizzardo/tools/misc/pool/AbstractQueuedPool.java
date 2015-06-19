package com.wizzardo.tools.misc.pool;

import java.io.IOException;
import java.util.Queue;

/**
 * Created by wizzardo on 18.06.15.
 */
public abstract class AbstractQueuedPool<T> implements Pool<T> {

    protected abstract Queue<Holder<T>> queue();

    @Override
    public T get() {
        T value;
        do {
            Holder<T> item = queue().poll();
            if (item == null)
                return create();

            value = item.get();
        } while (value == null);

        return value;
    }

    @Override
    public Holder<T> holder() {
        Holder<T> item = queue().poll();
        if (item == null)
            return createHolder(create());

        return item;
    }

    @Override
    public void release(T t) {
        release(createHolder(t));
    }

    public void release(Holder<T> holder) {
        queue().add(holder);
    }

    protected Holder<T> createHolder(T t) {
        return new SimpleHolder(t);
    }

    protected class SimpleHolder implements Holder<T> {
        final T value;

        public SimpleHolder(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void close() {
            release(this);
        }
    }

}
