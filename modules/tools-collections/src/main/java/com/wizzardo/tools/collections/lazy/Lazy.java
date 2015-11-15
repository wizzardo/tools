package com.wizzardo.tools.collections.lazy;

import com.wizzardo.tools.collections.lazy.Command.*;

import java.util.*;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Lazy<A, B> extends AbstractLazy<A, B> {

    public static <T> Lazy<T, T> of(final Iterable<T> iterable) {
        return new Lazy<T, T>(new Command<T, T>() {
            @Override
            protected void start() {
                if (child == null)
                    return;

                for (T t : iterable) {
                    child.process(t);
                }

                child.end();
            }
        });
    }

    public static <T> Lazy<T, T> of(final T... array) {
        return new Lazy<T, T>(new Command<T, T>() {
            @Override
            protected void start() {
                if (child == null)
                    return;

                for (T t : array) {
                    child.process(t);
                }

                child.end();
            }
        });
    }

    Lazy(Command<A, B> command) {
        super(command);
    }

    public Lazy<B, B> reduce(final Reducer<B> reducer) {
        return new Lazy<B, B>(new ReduceCommand<B>(command, reducer));
    }

    public Lazy<B, B> filter(final Filter<B> filter) {
        return new Lazy<B, B>(new FilterCommand<B>(command, filter));
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
