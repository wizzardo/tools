package com.wizzardo.tools.misc;

import com.wizzardo.tools.interfaces.Supplier;

/**
 * Created by wizzardo on 06/07/16.
 */
public class Lazy<T> {
    private volatile Holder<T> holder;

    public Lazy(final Supplier<T> supplier) {
        holder = new HolderWithSupplier<T>(new Supplier<T>() {
            @Override
            public T supply() {
                synchronized (Lazy.this) {
                    if (holder.getClass() != Holder.class)
                        holder = new Holder<T>(supplier.supply());
                }
                return holder.value;
            }
        });
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<T>(supplier);
    }

    public T get() {
        return holder.get();
    }

    static class Holder<T> {
        final T value;

        Holder(T value) {
            this.value = value;
        }

        T get() {
            return value;
        }
    }

    static class HolderWithSupplier<T> extends Holder<T> {
        Supplier<T> supplier;

        HolderWithSupplier(Supplier<T> supplier) {
            super(null);
            this.supplier = supplier;
        }

        @Override
        T get() {
            return supplier.supply();
        }
    }
}
