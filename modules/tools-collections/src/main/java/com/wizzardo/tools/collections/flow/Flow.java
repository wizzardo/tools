package com.wizzardo.tools.collections.flow;

import java.util.*;

/**
 * Created by wizzardo on 08.11.15.
 */
class Flow<A, B> {
    protected Flow<?, A> parent;
    protected Flow<B, ?> child;

    protected void process(A a) {
    }

    protected void processToChild(B b) {
        child.process(b);
    }

    protected <T extends Flow<B, C>, C> T then(T command) {
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

    public static final Mapper FLOW_GROUP_LIST_MAPPER = new Mapper<FlowGroup, List>() {
        @Override
        public List map(FlowGroup flowGroup) {
            return flowGroup.toList();
        }
    };

    public static <K, V> Mapper<FlowGroup<K, V>, List<V>> flowGroupListMapper() {
        return FLOW_GROUP_LIST_MAPPER;
    }


    public B reduce(Reducer<B> reducer) {
        return reduce(null, reducer);
    }

    public B reduce(B def, Reducer<B> reducer) {
        FlowReduce<B> reduce = then(new FlowReduce<B>(def, reducer));
        reduce.start();
        return reduce.get();
    }

    public <T> Flow<B, T> merge(Mapper<? super B, ? extends Flow<T, T>> mapper) {
        return then(new FlowMapMerge<B, T>(mapper));
    }

    public Flow<B, B> filter(Filter<? super B> filter) {
        return then(new FlowFilter<B>(filter));
    }

    public Flow<B, B> each(Consumer<? super B> consumer) {
        return then(new FlowEach<B>(consumer));
    }

    public Flow<B, B> each(ConsumerWithInt<? super B> consumer) {
        return then(new FlowEachWithIndex<B>(consumer));
    }

    public <T> Flow<B, T> map(Mapper<? super B, T> mapper) {
        return then(new FlowMap<B, T>(mapper));
    }

    public <K> FlowGrouping<K, B, B, FlowGroup<K, B>> groupBy(final Mapper<? super B, K> toKey) {
        return groupBy(toKey, Flow.<K, FlowGroup<K, B>>hashMapSupplier());
    }

    public <K> FlowGrouping<K, B, B, FlowGroup<K, B>> groupBy(final Mapper<? super B, K> toKey, final Supplier<Map<K, FlowGroup<K, B>>> groupMapSupplier) {
        return this.then(new FlowGrouping<K, B, B, FlowGroup<K, B>>(groupMapSupplier.supply()) {

            @Override
            protected void process(B b) {
                K key = toKey.map(b);
                FlowGroup<K, B> group = groups.get(key);
                if (group == null) {
                    groups.put(key, group = new FlowGroup<K, B>(key));
                    processToChild(group);
                }
                group.process(b);
            }

            @Override
            protected void onEnd() {
                for (FlowGroup<K, B> group : groups.values()) {
                    group.onEnd();
                }
                super.onEnd();
            }
        });
    }

    public <C> C collect(C collector, BiConsumer<? super C, ? super B> accumulator) {
        return then(new FlowCollectWithAccumulator<C, B>(collector, accumulator)).startAndGet();
    }

    public void execute() {
        then(new FinishFlow<B, B>()).start();
    }

    public int count() {
        FlowCount<B> count = then(new FlowCount<B>());
        count.start();
        return count.getCount();
    }

    public B first() {
        return first(null);
    }

    public B first(B def) {
        return then(new FlowFirst<B>(def)).startAndGet();
    }

    public B last() {
        return last(null);
    }

    public B last(B def) {
        return then(new FlowLast<B>(def)).startAndGet();
    }

    public B min(Comparator<? super B> comparator) {
        return min(null, comparator);
    }

    public B min(B def, Comparator<? super B> comparator) {
        return then(new FlowMinWithComparator<B>(def, comparator)).startAndGet();
    }

    public B min() {
        return min((B) null);
    }

    public B min(B def) {
        return then(new FlowMin<B>(def)).startAndGet();
    }

    public B max(Comparator<? super B> comparator) {
        return max(null, comparator);
    }

    public B max(B def, Comparator<? super B> comparator) {
        return then(new FlowMaxWithComparator<B>(def, comparator)).startAndGet();
    }

    public B max() {
        return max((B) null);
    }

    public B max(B def) {
        return then(new FlowMax<B>(def)).startAndGet();
    }

    public List<B> toList() {
        return collect(new ArrayList<B>());
    }

    public <C extends Collection<B>> C collect(C collection) {
        return then(new FlowCollect<B, C>(collection)).startAndGet();
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
        return then(new FlowJoin<B>(sb, separator)).startAndGet();
    }

    public <K, V> Map<K, V> toMap(Mapper<B, K> toKey, Mapper<FlowGroup<K, B>, V> toValue) {
        return toMap(Flow.<K, FlowGroup<K, B>>hashMapSupplier(), toKey, toValue);
    }

    public <K> Map<K, List<B>> toMap(Supplier<Map<K, FlowGroup<K, B>>> groupMapSupplier, Mapper<B, K> toKey) {
        return toMap(groupMapSupplier, toKey, Flow.<K, B>flowGroupListMapper());
    }

    public <K, V> Map<K, V> toMap(Supplier<Map<K, FlowGroup<K, B>>> groupMapSupplier, Mapper<B, K> toKey, Mapper<FlowGroup<K, B>, V> toValue) {
        return groupBy(toKey, groupMapSupplier).toMap(toValue);
    }


    public static <T> Flow<T, T> of(final Iterable<T> iterable) {
        return new FlowStart<T>() {
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

    public static <T> Flow<T, T> of(final Iterator<T> iterator) {
        return new FlowStart<T>() {
            @Override
            protected void process() {
                Iterator<T> i = iterator;
                Flow<T, ?> child = this.child;
                while (!stop && i.hasNext()) {
                    child.process(i.next());
                }
            }
        };
    }

    public static <K, V> Flow<Map.Entry<K, V>, Map.Entry<K, V>> of(Map<K, V> map) {
        return of(map.entrySet());
    }

    public static <T> Flow<T, T> of(final T... array) {
        return new FlowStart<T>() {
            @Override
            protected void process() {
                Flow<T, ?> child = this.child;
                for (T t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Integer, Integer> of(final int[] array) {
        return new FlowStart<Integer>() {
            @Override
            protected void process() {
                Flow<Integer, ?> child = this.child;
                for (int t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Long, Long> of(final long[] array) {
        return new FlowStart<Long>() {
            @Override
            protected void process() {
                Flow<Long, ?> child = this.child;
                for (long t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Double, Double> of(final double[] array) {
        return new FlowStart<Double>() {
            @Override
            protected void process() {
                Flow<Double, ?> child = this.child;
                for (double t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Float, Float> of(final float[] array) {
        return new FlowStart<Float>() {
            @Override
            protected void process() {
                Flow<Float, ?> child = this.child;
                for (float t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Byte, Byte> of(final byte[] array) {
        return new FlowStart<Byte>() {
            @Override
            protected void process() {
                Flow<Byte, ?> child = this.child;
                for (byte t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Boolean, Boolean> of(final boolean[] array) {
        return new FlowStart<Boolean>() {
            @Override
            protected void process() {
                Flow<Boolean, ?> child = this.child;
                for (boolean t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Short, Short> of(final short[] array) {
        return new FlowStart<Short>() {
            @Override
            protected void process() {
                Flow<Short, ?> child = this.child;
                for (short t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Character, Character> of(final char[] array) {
        return new FlowStart<Character>() {
            @Override
            protected void process() {
                Flow<Character, ?> child = this.child;
                for (char t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    private static abstract class FlowStart<T> extends Flow<T, T> {
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

    protected Flow getLast(Flow flow) {
        Flow last = flow;
        while (last.child != null) {
            last = last.child;
        }
        return last;
    }

    static class NoopFlow<T> extends Flow<T, Object> {
        @Override
        protected void onEnd() {
        }
    }

    static class FinishFlow<A, B> extends Flow<A, B> {

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

    static class FlowFilter<T> extends Flow<T, T> {
        private Filter<? super T> filter;

        public FlowFilter(Filter<? super T> filter) {
            this.filter = filter;
        }

        @Override
        protected void process(T t) {
            if (filter.allow(t))
                processToChild(t);
        }
    }

    static class FlowCollectWithAccumulator<C, T> extends FinishFlow<T, C> {
        private BiConsumer<? super C, ? super T> accumulator;
        private C collector;

        public FlowCollectWithAccumulator(C collector, BiConsumer<? super C, ? super T> accumulator) {
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

    static class FlowEach<T> extends Flow<T, T> {
        private Consumer<? super T> consumer;

        public FlowEach(Consumer<? super T> consumer) {
            this.consumer = consumer;
        }

        @Override
        protected void process(T t) {
            consumer.consume(t);
            processToChild(t);
        }
    }


    static class FlowEachWithIndex<T> extends Flow<T, T> {
        private ConsumerWithInt<? super T> consumer;
        private int index = 0;

        public FlowEachWithIndex(ConsumerWithInt<? super T> consumer) {
            this.consumer = consumer;
        }

        @Override
        protected void process(T t) {
            consumer.consume(index++, t);
            processToChild(t);
        }
    }

    static class FlowMerge<B extends Flow<T, T>, T> extends Flow<B, T> {
        final Flow<T, T> proxy = new Flow<T, T>() {
            @Override
            protected void process(T t) {
                FlowMerge.this.processToChild(t);
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

    static class FlowMapMerge<A, B> extends Flow<A, B> {
        final Flow<B, B> proxy = new Flow<B, B>() {
            @Override
            protected void process(B b) {
                FlowMapMerge.this.processToChild(b);
            }

            @Override
            protected void onEnd() {
            }
        };
        final Mapper<? super A, ? extends Flow<B, B>> mapper;

        FlowMapMerge(Mapper<? super A, ? extends Flow<B, B>> mapper) {
            this.mapper = mapper;
        }


        @Override
        protected void process(A a) {
            Flow<B, B> l = mapper.map(a);
            l.child = proxy;
            l.start();
        }
    }

    static class FlowMap<A, B> extends Flow<A, B> {
        private Mapper<? super A, B> mapper;

        public FlowMap(Mapper<? super A, B> mapper) {
            this.mapper = mapper;
        }

        @Override
        protected void process(A a) {
            processToChild(mapper.map(a));
        }
    }

    static class FlowReduce<T> extends FinishFlow<T, T> {
        private final Reducer<T> reducer;
        private T prev;

        public FlowReduce(T def, Reducer<T> reducer) {
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

    static class FlowCount<A> extends FinishFlow<A, Integer> {
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

    static class FlowCollect<A, C extends Collection<A>> extends FinishFlow<A, C> {
        private C collection;

        FlowCollect(C collection) {
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

    static class FlowFirst<A> extends FinishFlow<A, A> {
        private A first;

        public FlowFirst(A def) {
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

    static class FlowLast<A> extends FinishFlow<A, A> {
        private A last;

        public FlowLast(A def) {
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

    static class FlowMin<A> extends FinishFlow<A, A> {
        private A min;

        public FlowMin(A def) {
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

    static class FlowMax<A> extends FinishFlow<A, A> {
        private A max;

        public FlowMax(A def) {
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

    static class FlowJoin<A> extends FinishFlow<A, String> {
        private StringBuilder sb;
        private String separator;

        public FlowJoin(StringBuilder sb, String separator) {
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

    static class FlowMinWithComparator<A> extends FinishFlow<A, A> {
        private A min;
        private Comparator<? super A> comparator;

        FlowMinWithComparator(A def, Comparator<? super A> comparator) {
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

    static class FlowMaxWithComparator<A> extends FinishFlow<A, A> {
        private A max;
        private Comparator<? super A> comparator;

        FlowMaxWithComparator(A def, Comparator<? super A> comparator) {
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
}
