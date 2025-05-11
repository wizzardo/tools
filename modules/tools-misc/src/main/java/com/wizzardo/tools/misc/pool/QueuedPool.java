package com.wizzardo.tools.misc.pool;


import com.wizzardo.tools.interfaces.Consumer;
import com.wizzardo.tools.interfaces.Supplier;

import java.util.Queue;

/**
 * Created by wizzardo on 09/07/16.
 */
public class QueuedPool<T> extends AbstractQueuedPool<T> {
    protected final Supplier<T> supplier;
    protected final Supplier<Queue<Holder<T>>> queueSupplier;
    protected final PoolBuilder.HolderSupplier<T> holderSupplier;
    protected final Consumer<T> resetter;

    public QueuedPool(Supplier<T> supplier, Supplier<Queue<Holder<T>>> queueSupplier, PoolBuilder.HolderSupplier<T> holderSupplier, Consumer<T> resetter) {
        this.supplier = supplier;
        this.queueSupplier = queueSupplier;
        this.holderSupplier = holderSupplier;
        this.resetter = resetter;
    }

    @Override
    public T create() {
        return supplier.supply();
    }

    @Override
    protected Queue<Holder<T>> queue() {
        return queueSupplier.supply();
    }

    @Override
    protected Holder<T> createHolder(T t) {
        return holderSupplier.get(this, t);
    }

    @Override
    public T reset(T t) {
        resetter.consume(t);
        return t;
    }

    @Override
    public void dispose(Holder<T> h) {
    }
}
