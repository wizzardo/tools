package com.wizzardo.tools.collections.lazy;

import java.util.Map;

/**
 * Created by wizzardo on 08.11.15.
 */
public class LazyGrouping<K, T, A, B extends LazyGroup<K, T, T>> extends AbstractLazy<A, B> {

    protected final Map<K, LazyGroup<K, T, T>> groups;

    LazyGrouping(Map<K, LazyGroup<K, T, T>> groups) {
        this.groups = groups;
    }

    public <V> Lazy<V, V> flatMap(Mapper<? super B, V> mapper) {
        LazyContinue<V> continueCommand = new LazyContinue<V>(this);
        then(new GroupCommand<B, V>(mapper, continueCommand));
        return continueCommand;
    }

    public <V> Map<K, V> toMap(Mapper<? super B, V> mapper) {
        ToMapCommand<V> toMap;
        then(toMap = new ToMapCommand<V>((Map<K, V>) groups, mapper));
        toMap.start();
        return toMap.get();
    }

    @Override
    public LazyGrouping<K, T, B, B> filter(final Filter<? super B> filter) {
        return this.then(new LazyGrouping<K, T, B, B>(groups) {
            @Override
            protected void process(B b) {
                if (filter.allow(b))
                    child.process(b);
            }
        });
    }

    @Override
    public LazyGrouping<K, T, B, B> each(final Consumer<? super B> consumer) {
        return this.then(new LazyGrouping<K, T, B, B>(groups) {
            @Override
            protected void process(B b) {
                consumer.consume(b);
                child.process(b);
            }
        });
    }

    public Lazy<B, T> merge() {
        return then(new LazyMerge<B, T>());
    }

    private class GroupCommand<B extends LazyGroup<K, T, T>, V> extends Command<B, B> {
        private final Mapper<? super B, V> mapper;
        private final Command<V, V> continueCommand;

        public GroupCommand(Mapper<? super B, V> mapper, Command<V, V> continueCommand) {
            this.mapper = mapper;
            this.continueCommand = continueCommand;
        }

        @Override
        protected void process(B b) {
            mapper.map(b);
            getLast(b).then(new LazyOnEnd<V>(continueCommand));
        }

        @Override
        protected void onEnd() {
        }
    }

    private class ToMapCommand<V> extends FinishCommand<B, Map<K, V>> {
        private Map<K, V> groups;
        private final Mapper<? super B, V> mapper;

        public ToMapCommand(Map<K, V> groups, Mapper<? super B, V> mapper) {
            this.groups = groups;
            this.mapper = mapper;
        }

        @Override
        protected void process(final B b) {
            mapper.map(b);
            getLast(b).then(new LazyOnEnd<V>(new Command<V, V>() {
                @Override
                protected void process(V v) {
                    groups.put(b.getKey(), v);
                }
            }));
        }

        @Override
        protected Map<K, V> get() {
            return groups;
        }
    }

    private static class LazyContinue<T> extends Lazy<T, T> {
        private Command<?, ?> command;

        LazyContinue(Command<?, ?> command) {
            this.command = command;
        }

        @Override
        protected void start() {
            command.start();
        }

        @Override
        protected void stop() {
            command.stop();
        }

        @Override
        protected void process(T t) {
            child.process(t);
        }
    }

    private static class LazyOnEnd<T> extends Command<T, T> {
        private Command<T, ?> command;

        private LazyOnEnd(Command<T, ?> command) {
            this.command = command;
        }

        @Override
        protected void onEnd() {
            command.process(parent.get());
        }
    }
}
