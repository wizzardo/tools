package com.wizzardo.tools.collections.flow;

import java.util.*;

/**
 * Created by wizzardo on 08.11.15.
 */
class Command<A, B> {
    protected Command<?, A> parent;
    protected Command<B, ?> child;

    protected void process(A a) {
    }

    protected void processToChild(B b) {
        child.process(b);
    }

    protected <T extends Command<B, C>, C> T then(T command) {
        command.parent = this;
        this.child = command;
        return command;
    }

    protected void start() {
        parent.start();
    }

    protected void onEnd() {
        child.onEnd();
    }

    protected void stop() {
        parent.stop();
    }

    protected B get() {
        return null;
    }

    public static final Supplier SUPPLIER_HASH_MAP = new Supplier<Map>() {
        @Override
        public Map supply() {
            return new HashMap();
        }
    };

    public static <K, V> Supplier<Map<K, V>> hashMapSupplier() {
        return SUPPLIER_HASH_MAP;
    }


    public B reduce(Reducer<B> reducer) {
        return reduce(null, reducer);
    }

    public B reduce(B def, Reducer<B> reducer) {
        LazyReduce<B> reduce = then(new LazyReduce<B>(def, reducer));
        reduce.start();
        return reduce.get();
    }

    public <T> Command<B, T> merge(Mapper<? super B, ? extends Command<T, T>> mapper) {
        return then(new LazyMapMerge<B, T>(mapper));
    }

    public Command<B, B> filter(Filter<? super B> filter) {
        return then(new LazyFilter<B>(filter));
    }

    public Command<B, B> each(Consumer<? super B> consumer) {
        return then(new LazyEach<B>(consumer));
    }

    public Command<B, B> each(ConsumerWithInt<? super B> consumer) {
        return then(new LazyEachWithIndex<B>(consumer));
    }

    public <T> Command<B, T> map(Mapper<? super B, T> mapper) {
        return then(new LazyMap<B, T>(mapper));
    }

    public <K> LazyGrouping<K, B, B, LazyGroup<K, B>> groupBy(final Mapper<? super B, K> toKey) {
        return groupBy(toKey, Command.<K, LazyGroup<K, B>>hashMapSupplier());
    }

    public <K> LazyGrouping<K, B, B, LazyGroup<K, B>> groupBy(final Mapper<? super B, K> toKey, final Supplier<Map<K, LazyGroup<K, B>>> groupMapSupplier) {
        return this.then(new LazyGrouping<K, B, B, LazyGroup<K, B>>(groupMapSupplier.supply()) {

            @Override
            protected void process(B b) {
                K key = toKey.map(b);
                LazyGroup<K, B> group = groups.get(key);
                if (group == null) {
                    groups.put(key, group = new LazyGroup<K, B>(key));
                    processToChild(group);
                }
                group.process(b);
            }

            @Override
            protected void onEnd() {
                for (LazyGroup<K, B> group : groups.values()) {
                    group.onEnd();
                }
                super.onEnd();
            }
        });
    }

    public <C> C collect(C collector, BiConsumer<? super C, ? super B> accumulator) {
        return then(new LazyCollectWithAccumulator<C, B>(collector, accumulator)).startAndGet();
    }

    public void execute() {
        then(new Command.FinishCommand<B, B>()).start();
    }

    public int count() {
        LazyCount<B> count = then(new LazyCount<B>());
        count.start();
        return count.getCount();
    }

    public B first() {
        return first(null);
    }

    public B first(B def) {
        return then(new LazyFirst<B>(def)).startAndGet();
    }

    public B last() {
        return last(null);
    }

    public B last(B def) {
        return then(new LazyLast<B>(def)).startAndGet();
    }

    public B min(Comparator<? super B> comparator) {
        return min(null, comparator);
    }

    public B min(B def, Comparator<? super B> comparator) {
        return then(new LazyMinWithComparator<B>(def, comparator)).startAndGet();
    }

    public B min() {
        return min((B) null);
    }

    public B min(B def) {
        return then(new LazyMin<B>(def)).startAndGet();
    }

    public B max(Comparator<? super B> comparator) {
        return max(null, comparator);
    }

    public B max(B def, Comparator<? super B> comparator) {
        return then(new LazyMaxWithComparator<B>(def, comparator)).startAndGet();
    }

    public B max() {
        return max((B) null);
    }

    public B max(B def) {
        return then(new LazyMax<B>(def)).startAndGet();
    }

    public List<B> toList() {
        return collect(new ArrayList<B>());
    }

    public <C extends Collection<B>> C collect(C collection) {
        return then(new LazyCollect<B, C>(collection)).startAndGet();
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

    public String join(String separator) {
        return join(separator, new StringBuilder());
    }

    public String join(String separator, StringBuilder sb) {
        return then(new LazyJoin<B>(sb, separator)).startAndGet();
    }

    public <K, V> Map<K, V> toMap(Mapper<B, K> toKey, Mapper<LazyGroup<K, B>, V> toValue) {
        return toMap(Command.<K, LazyGroup<K, B>>hashMapSupplier(), toKey, toValue);
    }

    public <K> Map<K, List<B>> toMap(Supplier<Map<K, LazyGroup<K, B>>> groupMapSupplier, Mapper<B, K> toKey) {
        return toMap(groupMapSupplier, toKey, new LazyGroupToListMapper<K, B>());
    }

    public <K> Map<K, List<B>> toMapOfLists(Mapper<B, K> toKey) {
        return toMap(Command.<K, LazyGroup<K, B>>hashMapSupplier(), toKey, new LazyGroupToListMapper<K, B>());
    }

    public <K, V> Map<K, V> toMap(Supplier<Map<K, LazyGroup<K, B>>> groupMapSupplier, Mapper<B, K> toKey, Mapper<LazyGroup<K, B>, V> toValue) {
        return groupBy(toKey, groupMapSupplier).toMap(toValue);
    }


    public static <T> Command<T, T> of(final Iterable<T> iterable) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                for (T t : iterable) {
                    if (stop)
                        break;
                    processToChild(t);
                }
            }
        };
    }

    public static <T> Command<T, T> of(final Iterator<T> iterator) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                Iterator<T> i = iterator;
                Command<T, ?> child = this.child;
                while (!stop && i.hasNext()) {
                    child.process(i.next());
                }
            }
        };
    }

    public static <K, V> Command<Map.Entry<K, V>, Map.Entry<K, V>> of(Map<K, V> map) {
        return of(map.entrySet());
    }

    public static <T> Command<T, T> of(final T... array) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                Command<T, ?> child = this.child;
                for (T t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Command<Integer, Integer> of(final int[] array) {
        return new StartLazy<Integer>() {
            @Override
            protected void process() {
                Command<Integer, ?> child = this.child;
                for (int t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Command<Long, Long> of(final long[] array) {
        return new StartLazy<Long>() {
            @Override
            protected void process() {
                Command<Long, ?> child = this.child;
                for (long t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Command<Double, Double> of(final double[] array) {
        return new StartLazy<Double>() {
            @Override
            protected void process() {
                Command<Double, ?> child = this.child;
                for (double t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Command<Float, Float> of(final float[] array) {
        return new StartLazy<Float>() {
            @Override
            protected void process() {
                Command<Float, ?> child = this.child;
                for (float t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Command<Byte, Byte> of(final byte[] array) {
        return new StartLazy<Byte>() {
            @Override
            protected void process() {
                Command<Byte, ?> child = this.child;
                for (byte t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Command<Boolean, Boolean> of(final boolean[] array) {
        return new StartLazy<Boolean>() {
            @Override
            protected void process() {
                Command<Boolean, ?> child = this.child;
                for (boolean t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Command<Short, Short> of(final short[] array) {
        return new StartLazy<Short>() {
            @Override
            protected void process() {
                Command<Short, ?> child = this.child;
                for (short t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Command<Character, Character> of(final char[] array) {
        return new StartLazy<Character>() {
            @Override
            protected void process() {
                Command<Character, ?> child = this.child;
                for (char t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    private static abstract class StartLazy<T> extends Command<T, T> {
        boolean stop = false;

        @Override
        protected void start() {
            if (child == null)
                return;

            process();

            child.onEnd();
        }

        protected abstract void process();

        @Override
        protected void stop() {
            stop = true;
        }
    }

    protected Command getLast(Command command) {
        Command last = command;
        while (last.child != null) {
            last = last.child;
        }
        return last;
    }

    static class NoopCommand<T> extends Command<T, Object> {
        @Override
        protected void onEnd() {
        }
    }

    static class FinishCommand<A, B> extends Command<A, B> {

        @Override
        protected void onEnd() {
            if (child != null)
                child.onEnd();
        }

        public B startAndGet() {
            start();
            return get();
        }
    }

    static class LazyFilter<T> extends Command<T, T> {
        private Filter<? super T> filter;

        public LazyFilter(Filter<? super T> filter) {
            this.filter = filter;
        }

        @Override
        protected void process(T t) {
            if (filter.allow(t))
                processToChild(t);
        }
    }

    static class LazyCollectWithAccumulator<C, T> extends FinishCommand<T, C> {
        private BiConsumer<? super C, ? super T> accumulator;
        private C collector;

        public LazyCollectWithAccumulator(C collector, BiConsumer<? super C, ? super T> accumulator) {
            this.accumulator = accumulator;
            this.collector = collector;
        }

        @Override
        protected void process(T t) {
            accumulator.consume(collector, t);
        }

        @Override
        protected C get() {
            return collector;
        }
    }

    static class LazyEach<T> extends Command<T, T> {
        private Consumer<? super T> consumer;

        public LazyEach(Consumer<? super T> consumer) {
            this.consumer = consumer;
        }

        @Override
        protected void process(T t) {
            consumer.consume(t);
            processToChild(t);
        }
    }


    static class LazyEachWithIndex<T> extends Command<T, T> {
        private ConsumerWithInt<? super T> consumer;
        private int index = 0;

        public LazyEachWithIndex(ConsumerWithInt<? super T> consumer) {
            this.consumer = consumer;
        }

        @Override
        protected void process(T t) {
            consumer.consume(index++, t);
            processToChild(t);
        }
    }

    static class LazyMerge<B extends Command<T, T>, T> extends Command<B, T> {
        final Command<T, T> proxy = new Command<T, T>() {
            @Override
            protected void process(T t) {
                LazyMerge.this.processToChild(t);
            }

            @Override
            protected void onEnd() {
            }
        };

        @Override
        protected void process(B b) {
            b.child = proxy;
        }
    }

    static class LazyMapMerge<A, B> extends Command<A, B> {
        final Command<B, B> proxy = new Command<B, B>() {
            @Override
            protected void process(B b) {
                LazyMapMerge.this.processToChild(b);
            }

            @Override
            protected void onEnd() {
            }
        };
        final Mapper<? super A, ? extends Command<B, B>> mapper;

        LazyMapMerge(Mapper<? super A, ? extends Command<B, B>> mapper) {
            this.mapper = mapper;
        }


        @Override
        protected void process(A a) {
            Command<B, B> l = mapper.map(a);
            l.child = proxy;
            l.start();
        }
    }

    static class LazyMap<A, B> extends Command<A, B> {
        private Mapper<? super A, B> mapper;

        public LazyMap(Mapper<? super A, B> mapper) {
            this.mapper = mapper;
        }

        @Override
        protected void process(A a) {
            processToChild(mapper.map(a));
        }
    }

    static class LazyReduce<T> extends FinishCommand<T, T> {
        private final Reducer<T> reducer;
        private T prev;

        public LazyReduce(T def, Reducer<T> reducer) {
            this.reducer = reducer;
            prev = def;
        }

        @Override
        protected void process(T t) {
            if (prev == null)
                prev = t;
            else
                prev = reducer.reduce(prev, t);
        }

        @Override
        protected T get() {
            return prev;
        }
    }

    static class LazyCount<A> extends FinishCommand<A, Integer> {
        private int count = 0;

        @Override
        protected void process(A a) {
            count++;
        }

        @Override
        public Integer get() {
            return count;
        }

        public int getCount() {
            return count;
        }
    }

    static class LazyCollect<A, C extends Collection<A>> extends FinishCommand<A, C> {
        private C collection;

        LazyCollect(C collection) {
            this.collection = collection;
        }

        @Override
        protected void process(A a) {
            collection.add(a);
        }

        @Override
        public C get() {
            return collection;
        }
    }

    static class LazyFirst<A> extends FinishCommand<A, A> {
        private A first;

        public LazyFirst(A def) {
            first = def;
        }

        @Override
        protected void process(A a) {
            first = a;
            parent.stop();
            onEnd();
        }

        @Override
        public A get() {
            return first;
        }
    }

    static class LazyLast<A> extends FinishCommand<A, A> {
        private A last;

        public LazyLast(A def) {
            last = def;
        }

        @Override
        protected void process(A a) {
            last = a;
        }

        @Override
        public A get() {
            return last;
        }
    }

    static class LazyMin<A> extends FinishCommand<A, A> {
        private A min;

        public LazyMin(A def) {
            min = def;
        }

        @Override
        protected void process(A a) {
            if (min == null || ((Comparable<A>) min).compareTo(a) > 0)
                min = a;
        }

        @Override
        public A get() {
            return min;
        }
    }

    static class LazyMax<A> extends FinishCommand<A, A> {
        private A max;

        public LazyMax(A def) {
            max = def;
        }

        @Override
        protected void process(A a) {
            if (max == null || ((Comparable<A>) max).compareTo(a) < 0)
                max = a;
        }

        @Override
        public A get() {
            return max;
        }
    }

    static class LazyJoin<A> extends FinishCommand<A, String> {
        private StringBuilder sb;
        private String separator;

        public LazyJoin(StringBuilder sb, String separator) {
            this.sb = sb;
            this.separator = separator;
        }

        @Override
        protected void process(A a) {
            StringBuilder sb = this.sb;
            if (sb.length() > 0)
                sb.append(separator);

            sb.append(a);
        }

        @Override
        protected String get() {
            return sb.toString();
        }
    }

    static class LazyMinWithComparator<A> extends FinishCommand<A, A> {
        private A min;
        private Comparator<? super A> comparator;

        LazyMinWithComparator(A def, Comparator<? super A> comparator) {
            this.comparator = comparator;
            min = def;
        }

        @Override
        protected void process(A a) {
            if (min == null || comparator.compare(min, a) > 0)
                min = a;
        }

        @Override
        public A get() {
            return min;
        }
    }

    static class LazyMaxWithComparator<A> extends FinishCommand<A, A> {
        private A max;
        private Comparator<? super A> comparator;

        LazyMaxWithComparator(A def, Comparator<? super A> comparator) {
            this.comparator = comparator;
            max = def;
        }

        @Override
        protected void process(A a) {
            if (max == null || comparator.compare(max, a) < 0)
                max = a;
        }

        @Override
        public A get() {
            return max;
        }
    }

    static class LazyGroupToListMapper<K, B> implements Mapper<LazyGroup<K, B>, List<B>> {
        @Override
        public List<B> map(LazyGroup<K, B> group) {
            return group.toList();
        }
    }
}
