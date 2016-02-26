package com.wizzardo.tools.collections.lazy;

/**
 * Created by wizzardo on 08.11.15.
 */
public class LazyGrouping<K, T, A, B extends LazyGroup<K, T, T>> extends AbstractLazy<A, B> {

    public <V> Lazy<V, V> flatMap(Mapper<? super B, V> mapper) {
        final LazyContinue<V> continueCommand = new LazyContinue<V>(this);

        then(new GroupCommand<B, V>(mapper, continueCommand));

        return continueCommand;
    }

    @Override
    public LazyGrouping<K, T, B, B> filter(final Filter<? super B> filter) {
        return this.then(new LazyGrouping<K, T, B, B>() {
            @Override
            protected void process(B b) {
                if (filter.allow(b))
                    child.process(b);
            }
        });
    }

    @Override
    public LazyGrouping<K, T, B, B> each(final Consumer<? super B> consumer) {
        return this.then(new LazyGrouping<K, T, B, B>() {
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
