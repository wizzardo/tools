package com.wizzardo.tools.collections.lazy;

/**
 * Created by wizzardo on 08.11.15.
 */
public class LazyGrouping<K, T, A, B extends LazyGroup<K, T, T>> extends AbstractLazy<A, B> {
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

    @Override
    public LazyGrouping<K, T, B, B> filter(Filter<B> filter) {
        return new LazyGrouping<K, T, B, B>(new Command.FilterCommand<B>(command, filter));
    }

    @Override
    public LazyGrouping<K, T, B, B> each(Consumer<B> consumer) {
        return new LazyGrouping<K, T, B, B>(new Command.EachCommand<B>(command, consumer));
    }
}
