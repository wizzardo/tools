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
        return new LazyGrouping<K, B, B, LazyGroup<K, B, B>>(command.then(new Command<B, LazyGroup<K, B, B>>() {
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

                        @Override
                        protected void end() {
                            if (child != null)
                                child.end();
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
        }));
    }

    public void execute() {
        command.then(new Command.FinishCommand<B, B>()).start();
    }

    public int count() {
        Command.CountCommand<B> count;
        command.then(count = new Command.CountCommand<B>());
        count.start();
        return count.getCount();
    }

    public B first() {
        Command<B, B> c = command.then(new Command.FirstCommand<B>());
        c.start();
        return c.get();
    }

    public B last() {
        Command<B, B> c = command.then(new Command.LastCommand<B>());
        c.start();
        return c.get();
    }

    public B min(Comparator<B> comparator) {
        Command<B, B> c = command.then(new Command.MinWithComparatorCommand<B>(comparator));
        c.start();
        return c.get();
    }

    public B min() {
        Command<B, B> c = command.then(new Command.MinCommand<B>());
        c.start();
        return c.get();
    }

    public B max(Comparator<B> comparator) {
        Command<B, B> c = command.then(new Command.MaxWithComparatorCommand<B>(comparator));
        c.start();
        return c.get();
    }

    public B max() {
        Command<B, B> c = command.then(new Command.MaxCommand<B>());
        c.start();
        return c.get();
    }

    public List<B> toList() {
        return toList(INITIAL_LIST_SIZE);
    }

    public List<B> toList(int initialSize) {
        Command.CollectListCommand<B> c;
        command.then(c = new Command.CollectListCommand<B>(initialSize));
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
