package com.wizzardo.tools.collections.lazy;

import java.util.*;

/**
 * Created by wizzardo on 15.11.15.
 */
public abstract class AbstractLazy<A, B> {
    protected Command<A, B> command;

    AbstractLazy(Command<A, B> command) {
        this.command = command;
    }

    public abstract AbstractLazy<B, B> filter(final Filter<B> filter);

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

    public List<B> toList() {
        Command.CollectListCommand<B> c = new Command.CollectListCommand<B>(command);
        c.start();
        return c.get();
    }

    public List<B> toSortedList(Comparator<B> comparator) {
        Command.CollectListCommand<B> c = new Command.CollectListCommand<B>(command);
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

}
