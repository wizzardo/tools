package com.wizzardo.tools.collections.lazy;

import com.wizzardo.tools.collections.lazy.Command.*;

import java.util.*;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Lazy<A, B> {
    protected Command<A, B> command;

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
        this.command = command;
    }

    public Lazy<B, B> filter(final Filter<B> filter) {
        return new Lazy<B, B>(new FilterCommand<B>(command, filter));
    }

    public Lazy<B, B> reduce(final Reducer<B> reducer) {
        return new Lazy<B, B>(new Command<B, B>(command) {
            private B prev;

            @Override
            protected void process(B t) {
                if (prev == null)
                    prev = t;
                else
                    prev = reducer.reduce(prev, t);
            }

            @Override
            protected void end() {
                child.process(prev);
                super.end();
            }
        });
    }

    public <K> LazyGrouping<K, B, B, LazyGroup<K, B, B>> groupBy(final Mapper<B, K> toKey) {
        return new LazyGrouping<K, B, B, LazyGroup<K, B, B>>(new Command<B, LazyGroup<K, B, B>>(command) {
            Map<K, LazyGroup<K, B, B>> groups = new HashMap<K, LazyGroup<K, B, B>>();

            @Override
            protected void process(B b) {
                K key = toKey.map(b);
                LazyGroup<K, B, B> group = groups.get(key);
                if (group == null) {
                    groups.put(key, group = new LazyGroup<K, B, B>(key, new Command<B, B>() {
                        @Override
                        protected void process(B b) {
                            if (child != null)
                                child.process(b);
                        }

                        @Override
                        protected void start() {
                        }
                    }));
                    child.process(group);
                }
                group.command.process(b);
            }

            @Override
            protected void end() {
                for (LazyGroup<K, B, B> group : groups.values()) {
                    group.command.end();
                }
                super.end();
            }
        });
    }

    public <T> Lazy<B, T> map(final Mapper<B, T> mapper) {
        return new Lazy<B, T>(new Command<B, T>(command) {
            @Override
            protected void process(B b) {
                child.process(mapper.map(b));
            }
        });
    }

    public int count() {
        CountCommand<B> count = new CountCommand<B>(command);
        count.start();
        return count.getCount();
    }

    public B first() {
        FirstCommand<B> c = new FirstCommand<B>(command);
        c.start();
        return c.get();
    }

    public List<B> toList() {
        CollectListCommand<B> c = new CollectListCommand<B>(command);
        c.start();
        return c.get();
    }

    public List<B> toSortedList(Comparator<B> comparator) {
        CollectListCommand<B> c = new CollectListCommand<B>(command);
        c.start();
        List<B> list = c.get();
        Collections.sort(list, comparator);
        return list;
    }

    Command getLast(Command command) {
        Command last = command;
        while (last.child != null) {
            last = last.child;
        }
        return last;
    }

    interface Filter<T> {
        boolean allow(T t);
    }

    interface Reducer<T> {
        T reduce(T t1, T t2);
    }

    interface Mapper<A, B> {
        B map(A a);
    }
}
