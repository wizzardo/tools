package com.wizzardo.tools.collections.lazy;

import java.util.Collection;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Lazy<A, B> extends AbstractLazy<A, B> {

    private static final Iterater<Collection, Object> COLLECTION_ITERATOR = new Iterater<Collection, Object>() {
        @Override
        public void iterate(Collection collection, Consumer<Object> consumer) {
            for (Object o : collection) {
                consumer.consume(o);
            }
        }
    };

    public static <T> Lazy<T, T> of(final Iterable<T> iterable) {
        return new Lazy<T, T>() {
            boolean stop = false;

            @Override
            protected void start() {
                if (child == null)
                    return;

                for (T t : iterable) {
                    child.process(t);
                    if (stop)
                        break;
                }

                child.end();
            }

            @Override
            protected void stop() {
                stop = true;
            }
        };
    }

    public static <T> Lazy<T, T> of(final T... array) {
        return new Lazy<T, T>() {
            boolean stop = false;

            @Override
            protected void start() {
                if (child == null)
                    return;

                for (T t : array) {
                    child.process(t);
                    if (stop)
                        break;
                }

                child.end();
            }

            @Override
            protected void stop() {
                stop = true;
            }
        };
    }

    public B reduce(Reducer<B> reducer) {
        return reduce(null, reducer);
    }

    public B reduce(B def, Reducer<B> reducer) {
        LazyReduce<B> reduce = then(new LazyReduce<B>(def, reducer));
        reduce.start();
        return reduce.get();
    }

    public Lazy<B, B> filter(Filter<? super B> filter) {
        return then(new LazyFilter<B>(filter));
    }

    public Lazy<B, B> each(Consumer<? super B> consumer) {
        return then(new LazyEach<B>(consumer));
    }

    public <T> Lazy<B, T> iterate(Iterater<? super B, T> iterater) {
        return then(new LazyIterate<B, T>(iterater));
    }

    public <T> Lazy<B, T> iterate() {
        return then(new LazyIterate<B, T>((Iterater<? super B, T>) COLLECTION_ITERATOR));
    }

    public <T> Lazy<B, T> map(Mapper<? super B, T> mapper) {
        return then(new LazyMap<B, T>(mapper));
    }
}
