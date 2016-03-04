package com.wizzardo.tools.collections.lazy;

import java.util.*;

/**
 * Created by wizzardo on 15.11.15.
 */
public abstract class AbstractLazy<A, B> extends Command<A, B> {
    public static final Supplier SUPPLIER_HASH_MAP = new Supplier<Map>() {
        @Override
        public Map supply() {
            return new HashMap();
        }
    };

    public abstract AbstractLazy<B, B> filter(Filter<? super B> filter);

    public abstract AbstractLazy<B, B> each(Consumer<? super B> consumer);

    public <K> LazyGrouping<K, B, B, LazyGroup<K, B, B>> groupBy(final Mapper<? super B, K> toKey) {
        return groupBy(toKey, SUPPLIER_HASH_MAP);
    }

    public <K> LazyGrouping<K, B, B, LazyGroup<K, B, B>> groupBy(final Mapper<? super B, K> toKey, final Supplier<Map<K, LazyGroup<K, B, B>>> groupMapSupplier) {
        return this.then(new LazyGrouping<K, B, B, LazyGroup<K, B, B>>(groupMapSupplier.supply()) {

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
                        protected void onEnd() {
                            if (!stopped && child != null)
                                child.onEnd();
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
            protected void onEnd() {
                for (LazyGroup<K, B, B> group : groups.values()) {
                    group.onEnd();
                }
                super.onEnd();
            }
        });
    }

    public <C> C collect(C collector, BiConsumer<? super C, ? super B> accumulator) {
        LazyCollectWithAccumulator<C, B> collect;
        then(collect = new LazyCollectWithAccumulator<C, B>(collector, accumulator));
        collect.start();
        return collector;
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

    public B min(Comparator<? super B> comparator) {
        return min(null, comparator);
    }

    public B min(B def, Comparator<? super B> comparator) {
        Command<B, B> c = then(new LazyMinWithComparator<B>(def, comparator));
        c.start();
        return c.get();
    }

    public B min() {
        return min((B) null);
    }

    public B min(B def) {
        Command<B, B> c = then(new LazyMin<B>(def));
        c.start();
        return c.get();
    }

    public B max(Comparator<? super B> comparator) {
        return max(null, comparator);
    }

    public B max(B def, Comparator<? super B> comparator) {
        Command<B, B> c = then(new LazyMaxWithComparator<B>(def, comparator));
        c.start();
        return c.get();
    }

    public B max() {
        return max((B) null);
    }

    public B max(B def) {
        Command<B, B> c = then(new LazyMax<B>(def));
        c.start();
        return c.get();
    }

    public List<B> toList() {
        return collect(new ArrayList<B>());
    }

    public <C extends Collection<B>> C collect(C collection) {
        LazyCollect<B> c;
        then(c = new LazyCollect<B>(collection));
        c.start();
        return collection;
    }

    public List<B> toSortedList(Comparator<? super B> comparator) {
        return toSortedList(new ArrayList<B>(), comparator);
    }

    public List<B> toSortedList() {
        return toSortedList(new ArrayList<B>());
    }

    public List<B> toSortedList(List<B> list, Comparator<? super B> comparator) {
        Collections.sort(collect(list), comparator);
        return list;
    }

    public List<B> toSortedList(List<B> list) {
        Collections.sort((List<Comparable>) collect(list));
        return list;
    }

}
