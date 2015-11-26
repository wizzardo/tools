package com.wizzardo.tools.collections.lazy;

import com.wizzardo.tools.collections.lazy.Command.*;

import java.util.*;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Lazy<A, B> extends AbstractLazy<A, B> {

    public static <T> Lazy<T, T> of(final Iterable<T> iterable) {
        return new Lazy<T, T>(new Command<T, T>() {
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
        });
    }

    public static <T> Lazy<T, T> of(final T... array) {
        return new Lazy<T, T>(new Command<T, T>() {
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
        });
    }

    Lazy(Command<A, B> command) {
        super(command);
    }

    public Lazy<B, B> reduce(Reducer<B> reducer) {
        return lazy(new ReduceCommand<B>(reducer));
    }

    public Lazy<B, B> filter(Filter<B> filter) {
        return lazy(new FilterCommand<B>(filter));
    }

    public Lazy<B, B> each(Consumer<B> consumer) {
        return lazy(new EachCommand<B>(consumer));
    }

    public <T> Lazy<B, T> map(final Mapper<B, T> mapper) {
        return lazy(new MapCommand<B, T>(mapper));
    }

    private <C> Lazy<B, C> lazy(Command<B, C> next) {
        return new Lazy<B, C>(command.then(next));
    }
}
