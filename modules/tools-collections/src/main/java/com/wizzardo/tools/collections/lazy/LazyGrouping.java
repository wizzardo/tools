package com.wizzardo.tools.collections.lazy;

/**
 * Created by wizzardo on 08.11.15.
 */
public class LazyGrouping<K, T, A, B extends LazyGroup<K, T, T>> extends AbstractLazy<A, B> {
    LazyGrouping(Command<A, B> command) {
        super(command);
    }

    public <V> Lazy<V, V> flatMap(final Mapper<B, V> mapper) {
        final Command<V, V> continueCommand = new ContinueCommand<V>(command);

        new GroupCommand<B, V>(command, mapper, continueCommand);

        return new Lazy<V, V>(continueCommand);
    }

    @Override
    public LazyGrouping<K, T, B, B> filter(Filter<B> filter) {
        return new LazyGrouping<K, T, B, B>(new Command.FilterCommand<B>(command, filter));
    }

    @Override
    public LazyGrouping<K, T, B, B> each(Consumer<B> consumer) {
        return new LazyGrouping<K, T, B, B>(new Command.EachCommand<B>(command, consumer));
    }

    private class GroupCommand<B extends LazyGroup<K, T, T>, V> extends Command<B, B> {
        private final Mapper<B, V> mapper;
        private final Command<V, V> continueCommand;

        public GroupCommand(Command<?, B> parent, Mapper<B, V> mapper, Command<V, V> continueCommand) {
            super(parent);
            this.mapper = mapper;
            this.continueCommand = continueCommand;
        }

        @Override
        protected void process(B b) {
            mapper.map(b);
            new ProcessOnEndCommand<V>(getLast(b.command), continueCommand);
        }

        @Override
        protected void end() {
        }
    }

    private static class ContinueCommand<T> extends Command<T, T> {
        private Command<?, ?> command;

        ContinueCommand(Command<?, ?> command) {
            this.command = command;
        }

        @Override
        protected void start() {
            command.start();
        }

        @Override
        protected void process(T t) {
            child.process(t);
        }
    }

    private static class ProcessOnEndCommand<T> extends Command<T, T> {
        private Command<T, ?> command;

        private ProcessOnEndCommand(Command<?, T> parent, Command<T, ?> command) {
            super(parent);
            this.command = command;
        }

        @Override
        protected void end() {
            command.process(parent.get());
        }
    }
}
