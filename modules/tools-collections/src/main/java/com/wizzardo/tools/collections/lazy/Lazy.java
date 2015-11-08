package com.wizzardo.tools.collections.lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Lazy(Command<A, B> command) {
        this.command = command;
    }

    public Lazy<B, B> filter(final Filter<B> filter) {
        return new Lazy<B, B>(new Command<B, B>(command) {
            @Override
            protected void process(B t) {
                if (filter.allow(t))
                    child.process(t);
            }
        });
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

    public <K> LazyGrouping<K, B, LazyGroup<K, B, B>> groupBy(final Mapper<B, K> toKey) {
        return new LazyGrouping<K, B, LazyGroup<K, B, B>>(new Command<B, LazyGroup<K, B, B>>(command) {
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

    public static class LazyGroup<K, A, B> extends Lazy<A, B> {
        private K key;

        LazyGroup(K key, Command<A, B> command) {
            super(command);
            this.key = key;
        }

        public K getKey() {
            return key;
        }
    }

    public static class LazyGrouping<K, A, B extends LazyGroup<K, A, A>> extends Lazy<A, B> {
        LazyGrouping(Command<A, B> command) {
            super(command);
        }

        public <V> Lazy<V, V> flatMap(final Mapper<B, V> mapper) {
            final Command<V, V> main = new Command<V, V>() {
                @Override
                protected void start() {
                    command.start();
                }

                @Override
                protected void process(V v) {
                    child.process(v);
                }
            };


            new Command<B, B>(command) {
                @Override
                protected void process(B b) {
                    mapper.map(b);
                    new Command<V, V>(getLast(b.command)) {
                        @Override
                        protected void end() {
                            main.process(parent.get());
                        }
                    };
                }
            };

            return new Lazy<V, V>(main);
        }
    }

    Command getLast(Command command) {
        Command last = command;
        while (last.child != null) {
            last = last.child;
        }
        return last;
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
        return count.count;
    }

    public B first() {
        FirstCommand<B> c = new FirstCommand<B>(command);
        c.start();
        return c.first;
    }

    public List<B> toList() {
        CollectListCommand<B> c = new CollectListCommand<B>(command);
        c.start();
        return c.get();
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

    static abstract class Command<A, B> {
        Command<?, A> parent;
        Command<B, ?> child;

        Command(Command<?, A> parent) {
            this.parent = parent;
            parent.child = this;
        }

        Command() {
        }

        protected void process(A a) {
        }

        protected void start() {
            parent.start();
        }

        protected void end() {
            if (child != null)
                child.end();
        }

        public B get() {
            return null;
        }

        public boolean isBlocking() {
            return false;
        }
    }

    static class CountCommand<A> extends Command<A, Integer> {
        int count = 0;

        CountCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            count++;
        }

        @Override
        public Integer get() {
            return count;
        }
    }

    static class CollectListCommand<A> extends Command<A, List<A>> {
        List<A> list = new ArrayList<A>();

        CollectListCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            list.add(a);
        }

        @Override
        public List<A> get() {
            return list;
        }

        @Override
        public boolean isBlocking() {
            return true;
        }
    }

    static class NoopCommand<A> extends Command<A, A> {

        NoopCommand(Command<A, A> parent) {
            super(parent);
        }

        NoopCommand() {
        }


        @Override
        protected void process(A a) {
            child.process(a);
        }

        @Override
        public A get() {
            return null;
        }

        @Override
        protected void start() {
        }
    }

    static class FirstCommand<A> extends Command<A, A> {
        A first;

        FirstCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            if (first == null)
                first = a;
        }

        @Override
        public A get() {
            return first;
        }

    }
}
