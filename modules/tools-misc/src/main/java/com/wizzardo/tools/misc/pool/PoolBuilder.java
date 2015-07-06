package com.wizzardo.tools.misc.pool;

import com.wizzardo.tools.misc.Consumer;
import com.wizzardo.tools.misc.Supplier;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by wizzardo on 30.06.15.
 */
public class PoolBuilder<T> {

    protected Supplier<T> supplier = new Supplier<T>() {
        @Override
        public T supply() {
            return null;
        }
    };

    protected Consumer<T> resetter = new Consumer<T>() {
        @Override
        public void consume(T t) {
        }
    };

    protected HolderSupplier<T> holderSupplier = new HolderSupplier<T>() {
        @Override
        public Holder<T> get(Pool<T> pool, T value, final Consumer<T> resetter) {
            return new SoftHolder<T>(pool, value) {
                @Override
                public T get() {
                    T t = super.get();
                    resetter.consume(t);
                    return t;
                }
            };
        }
    };

    protected Supplier<Queue<Holder<T>>> queueSupplier = new Supplier<Queue<Holder<T>>>() {
        ThreadLocal<Queue<Holder<T>>> queue = new ThreadLocal<Queue<Holder<T>>>() {
            @Override
            protected Queue<Holder<T>> initialValue() {
                return new LinkedList<Holder<T>>();
            }
        };

        @Override
        public Queue<Holder<T>> supply() {
            return queue.get();
        }
    };

    public PoolBuilder<T> queue(Supplier<Queue<Holder<T>>> queueSupplier) {
        this.queueSupplier = queueSupplier;
        return this;
    }

    public PoolBuilder<T> holder(HolderSupplier<T> holderSupplier) {
        this.holderSupplier = holderSupplier;
        return this;
    }

    public PoolBuilder<T> supplier(Supplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }

    public PoolBuilder<T> resetter(Consumer<T> resetter) {
        this.resetter = resetter;
        return this;
    }

    public Pool<T> build() {
        return new AbstractQueuedPool<T>() {
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
                return holderSupplier.get(this, t, resetter);
            }
        };
    }

    public interface HolderSupplier<T> {
        Holder<T> get(Pool<T> pool, T value, Consumer<T> resetter);
    }
}
