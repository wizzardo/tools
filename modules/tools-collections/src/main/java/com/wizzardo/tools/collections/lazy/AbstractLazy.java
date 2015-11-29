package com.wizzardo.tools.collections.lazy;

import java.util.*;

/**
 * Created by wizzardo on 15.11.15.
 */
public abstract class AbstractLazy<A, B> extends Command<A, B> {
    protected static final int INITIAL_LIST_SIZE = 10;

    public abstract AbstractLazy<B, B> filter(Filter<B> filter);

    public abstract AbstractLazy<B, B> each(Consumer<B> consumer);

    public <K> LazyGrouping<K, B, B, LazyGroup<K, B, B>> groupBy(final Mapper<B, K> toKey) {
        return this.then(new LazyGrouping<K, B, B, LazyGroup<K, B, B>>() {
            Map<K, LazyGroup<K, B, B>> groups = new HashMap<K, LazyGroup<K, B, B>>();

            @Override
            protected void process(B b) {
                K key = toKey.map(b);
                LazyGroup<K, B, B> group = groups.get(key);
                if (group == null) {
                    groups.put(key, group = new LazyGroup<K, B, B>(key) {
                        boolean stopped = false;

                        @Override
                        protected void process(B b) {
                            if (stopped)
                                return;

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

                        @Override
                        protected void stop() {
                            stopped = true;
                        }
                    });
                    child.process(group);
                }
                group.process(b);
            }

            @Override
            protected void end() {
                for (LazyGroup<K, B, B> group : groups.values()) {
                    group.end();
                }
                super.end();
            }
        });
    }

    public void execute() {
        then(new Command.FinishCommand<B, B>()).start();
    }

    public int count() {
        LazyCount<B> count;
        then(count = new LazyCount<B>());
        count.start();
        return count.getCount();
    }

    public B first() {
        return first(null);
    }

    public B first(B def) {
        Command<B, B> c = then(new LazyFirst<B>(def));
        c.start();
        return c.get();
    }

    public B last() {
        return last(null);
    }

    public B last(B def) {
        Command<B, B> c = then(new LazyLast<B>(def));
        c.start();
        return c.get();
    }

    public B min(Comparator<B> comparator) {
        Command<B, B> c = then(new LazyMinWithComparator<B>(comparator));
        c.start();
        return c.get();
    }

    public B min() {
        Command<B, B> c = then(new LazyMin<B>());
        c.start();
        return c.get();
    }

    public B max(Comparator<B> comparator) {
        Command<B, B> c = then(new LazyMaxWithComparator<B>(comparator));
        c.start();
        return c.get();
    }

    public B max() {
        Command<B, B> c = then(new LazyMax<B>());
        c.start();
        return c.get();
    }

    public List<B> toList() {
        return toList(INITIAL_LIST_SIZE);
    }

    public List<B> toList(int initialSize) {
        LazyCollectList<B> c;
        then(c = new LazyCollectList<B>(initialSize));
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
