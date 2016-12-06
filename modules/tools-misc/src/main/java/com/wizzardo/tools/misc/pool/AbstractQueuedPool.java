package com.wizzardo.tools.misc.pool;

import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.misc.Unchecked;

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
            Holder<T> item = poll();
            if (item == null)
                return create();

            value = item.get();
        } while (value == null);

        return value;
    }

    protected Holder<T> poll() {
        return queue().poll();
    }

    @Override
    public Holder<T> holder() {
        Holder<T> item = poll();
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
        return new SimpleHolder<T>(this, t);
    }

    public <R> R provide(UnsafeMapper<T, R> consumer) {
        Holder<T> holder = poll();
        if (holder == null)
            holder = createHolder(create());

        try {
            return consumer.map(holder.get());
        } catch (Exception e) {
            throw Unchecked.rethrow(e);
        } finally {
            release(holder);
        }
    }

    public int size() {
        return queue().size();
    }
}
