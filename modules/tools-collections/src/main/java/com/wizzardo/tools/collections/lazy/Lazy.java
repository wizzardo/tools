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
        return new Lazy<B, B>(new ReduceCommand<B>(command, reducer));
    }

    public Lazy<B, B> filter(Filter<B> filter) {
        return new Lazy<B, B>(new FilterCommand<B>(command, filter));
    }

    public Lazy<B, B> each(Consumer<B> consumer) {
        return new Lazy<B, B>(new EachCommand<B>(command, consumer));
    }

    public <T> Lazy<B, T> map(final Mapper<B, T> mapper) {
        return new Lazy<B, T>(new Command<B, T>(command) {
            @Override
            protected void process(B b) {
                child.process(mapper.map(b));
            }
        });
    }
}
