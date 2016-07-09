package com.wizzardo.tools.misc.pool;

import com.wizzardo.tools.misc.Consumer;
import com.wizzardo.tools.misc.Supplier;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wizzardo on 30.06.15.
 */
public class PoolBuilder<T> {

    protected int initialSize = 0;
    protected int limitSize = 0;

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

    protected Supplier<Queue<Holder<T>>> queueSupplier;

    public static <T> Supplier<Queue<Holder<T>>> createThreadLocalQueueSupplier() {
        return new Supplier<Queue<Holder<T>>>() {
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
    }

    public static <T> Supplier<Queue<Holder<T>>> createSharedQueueSupplier() {
        return new Supplier<Queue<Holder<T>>>() {
            Queue<Holder<T>> queue = new ConcurrentLinkedQueue<Holder<T>>();

            @Override
            public Queue<Holder<T>> supply() {
                return queue;
            }
        };
    }

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

    public PoolBuilder<T> initialSize(int initialSize) {
        this.initialSize = initialSize;
        return this;
    }

    public PoolBuilder<T> limitSize(int limitSize) {
        this.limitSize = limitSize;
        return this;
    }

    public Pool<T> build() {
        if (queueSupplier == null)
            throw new IllegalArgumentException("queueSupplier must not be null");

        AbstractQueuedPool<T> pool;
        if (limitSize <= 0) {
            pool = new QueuedPool<T>(supplier, queueSupplier, holderSupplier, resetter);
        } else {
            pool = new QueuedPool<T>(supplier, queueSupplier, holderSupplier, resetter) {
                AtomicInteger created = new AtomicInteger();
                AtomicInteger waits = new AtomicInteger();

                @Override
                protected Holder<T> poll() {
                    Holder<T> holder = super.poll();
                    if (holder == null && created.get() == limitSize) {
                        synchronized (this) {
                            while ((holder = super.poll()) == null) {
                                waits.incrementAndGet();
                                try {
                                    this.wait();
                                } catch (InterruptedException ignored) {
                                }
                                waits.decrementAndGet();
                            }
                        }
                    }
                    return holder;
                }

                @Override
                public T create() {
                    created.incrementAndGet();
                    return super.create();
                }

                @Override
                public void release(Holder<T> holder) {
                    super.release(holder);
                    if (waits.get() > 0) {
                        synchronized (this) {
                            if (waits.get() > 0)
                                this.notify();
                        }
                    }
                }
            };
        }

        for (int i = 0; i < initialSize; i++) {
            pool.release(pool.create());
        }
        return pool;
    }

    public interface HolderSupplier<T> {
        Holder<T> get(Pool<T> pool, T value, Consumer<T> resetter);
    }
}
