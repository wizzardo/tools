package com.wizzardo.tools.collections.lazy;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Lazy<A, B> extends AbstractLazy<A, B> {

    private static final Iterater<Iterable, Object> ITERABLE_ITERATOR = new Iterater<Iterable, Object>() {
        @Override
        public void iterate(Iterable iterable, Consumer<Object> consumer) {
            for (Object o : iterable) {
                consumer.consume(o);
            }
        }
    };

    public static <T> Lazy<T, T> of(final Iterable<T> iterable) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                for (T t : iterable) {
                    child.process(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static <T> Lazy<T, T> of(final Iterator<T> iterator) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                while (!stop && iterator.hasNext()) {
                    child.process(iterator.next());
                }
            }
        };
    }

    public static <T> Lazy<T, T> of(final T... array) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                for (T t : array) {
                    child.process(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    private static abstract class StartLazy<T> extends Lazy<T, T> {
        boolean stop = false;

        @Override
        protected void start() {
            if (child == null)
                return;

            process();

            child.onEnd();
        }

        protected abstract void process();

        @Override
        protected void stop() {
            stop = true;
        }
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
        return then(new LazyIterate<B, T>((Iterater<? super B, T>) ITERABLE_ITERATOR));
    }

    public <T> Lazy<B, T> map(Mapper<? super B, T> mapper) {
        return then(new LazyMap<B, T>(mapper));
    }

    public <K> Map<K, List<B>> toMap(Mapper<B, K> toKey) {
        return toMap(Lazy.<K, LazyGroup<K, B, B>>hashMapSupplier(), toKey, new LazyGroupToListMapper<K, B>());
    }
}
