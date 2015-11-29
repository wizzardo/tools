package com.wizzardo.tools.collections.lazy;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Lazy<A, B> extends AbstractLazy<A, B> {

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

    public Lazy<B, B> reduce(Reducer<B> reducer) {
        return then(new LazyReduce<B>(reducer));
    }

    public Lazy<B, B> filter(Filter<B> filter) {
        return then(new LazyFilter<B>(filter));
    }

    public Lazy<B, B> each(Consumer<B> consumer) {
        return then(new LazyEach<B>(consumer));
    }

    public <T> Lazy<B, T> map(final Mapper<B, T> mapper) {
        return then(new LazyMap<B, T>(mapper));
    }
}
