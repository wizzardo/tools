package com.wizzardo.tools.collections.lazy;

import java.util.*;

/**
 * Created by wizzardo on 15.11.15.
 */
public abstract class AbstractLazy<A, B> {
    protected static final int INITIAL_LIST_SIZE = 10;
    protected Command<A, B> command;

    AbstractLazy(Command<A, B> command) {
        this.command = command;
    }

    public abstract AbstractLazy<B, B> filter(Filter<B> filter);

    public abstract AbstractLazy<B, B> each(Consumer<B> consumer);

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

    public void execute() {
        command.start();
    }

    public int count() {
        Command.CountCommand<B> count = new Command.CountCommand<B>(command);
        count.start();
        return count.getCount();
    }

    public B first() {
        Command.FirstCommand<B> c = new Command.FirstCommand<B>(command);
        c.start();
        return c.get();
    }

    public B last() {
        Command.LastCommand<B> c = new Command.LastCommand<B>(command);
        c.start();
        return c.get();
    }

    public List<B> toList() {
        return toList(INITIAL_LIST_SIZE);
    }

    public List<B> toList(int initialSize) {
        Command.CollectListCommand<B> c = new Command.CollectListCommand<B>(command, initialSize);
        c.start();
        return c.get();
    }

    public List<B> toSortedList(Comparator<B> comparator) {
        return toSortedList(INITIAL_LIST_SIZE, comparator);
    }

    public List<B> toSortedList() {
        return toSortedList(INITIAL_LIST_SIZE);
    }

    public List<B> toSortedList(int initialSize, Comparator<B> comparator) {
        List<B> list = toList(initialSize);
        Collections.sort(list, comparator);
        return list;
    }

    public List<B> toSortedList(int initialSize) {
        List<B> list = toList(initialSize);
        Collections.sort((List<Comparable>) list);
        return list;
    }

    Command getLast(Command command) {
        Command last = command;
        while (last.child != null) {
            last = last.child;
        }
        return last;
    }

}
