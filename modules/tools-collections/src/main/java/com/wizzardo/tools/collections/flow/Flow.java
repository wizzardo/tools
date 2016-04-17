package com.wizzardo.tools.collections.flow;

import com.wizzardo.tools.collections.flow.flows.*;

import java.util.*;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Flow<A, B> {
    protected Flow<?, A> parent;
    protected Flow<B, ?> child;

    public void process(A a) {
    }

    public <T extends Flow<B, C>, C> T then(T command) {
        command.parent = this;
        this.child = command;
        return command;
    }

    protected void start() {
        parent.start();
    }

    protected void start(Flow flow) {
        flow.start();
    }

    protected void setChildTo(Flow parent, Flow child) {
        parent.child = child;
    }

    protected void onEnd() {
        if (child != null)
            child.onEnd();
    }

    protected void onEnd(Flow group) {
        group.onEnd();
    }

    protected void stop() {
        parent.stop();
    }

    protected void stop(Flow flow) {
        flow.stop();
    }

    public B get() {
        return null;
    }

    protected Flow getLast(Flow flow) {
        Flow last = flow;
        while (last.child != null) {
            last = last.child;
        }
        return last;
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
        return then(new FlowReduce<B>(def, reducer)).startAndGet();
    }

    public <T> Flow<B, T> merge(Mapper<? super B, ? extends Flow<? extends T, ? extends T>> mapper) {
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

    /**
     * converts B into T with Mapper, ignores null-values
     */
    public <T> Flow<B, T> map(Mapper<? super B, T> mapper) {
        return then(new FlowMap<B, T>(mapper));
    }

    public <K> FlowGrouping<K, B, B, FlowGroup<K, B>> groupBy(final Mapper<? super B, K> toKey) {
        return groupBy(toKey, Flow.<K, FlowGroup<K, B>>hashMapSupplier());
    }

    public <K> FlowGrouping<K, B, B, FlowGroup<K, B>> groupBy(final Mapper<? super B, K> toKey, final Supplier<Map<K, FlowGroup<K, B>>> groupMapSupplier) {
        return this.then(new FlowGroupBy<K, B>(groupMapSupplier, toKey));
    }

    public <C> C collect(C collector, BiConsumer<? super C, ? super B> accumulator) {
        return then(new FlowCollectWithAccumulator<C, B>(collector, accumulator)).startAndGet();
    }

    public void execute() {
        start();
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

    public boolean anyMatch(Filter<B> filter) {
        return then(new FlowAnyMatch<B>(filter)).startAndGet();
    }

    public boolean allMatch(Filter<B> filter) {
        return then(new FlowAllMatch<B>(filter)).startAndGet();
    }

    public boolean noneMatch(Filter<B> filter) {
        return then(new FlowNoneMatch<B>(filter)).startAndGet();
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
        return FlowStart.of(iterable);
    }

    public static <T> Flow<T, T> of(final Iterator<T> iterator) {
        return FlowStart.of(iterator);
    }

    public static <K, V> Flow<Map.Entry<K, V>, Map.Entry<K, V>> of(Map<K, V> map) {
        return FlowStart.of(map.entrySet());
    }

    public static <T> Flow<T, T> of(final T... array) {
        return FlowStart.of(array);
    }

    public static Flow<Integer, Integer> of(final int[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Long, Long> of(final long[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Double, Double> of(final double[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Float, Float> of(final float[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Byte, Byte> of(final byte[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Boolean, Boolean> of(final boolean[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Short, Short> of(final short[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Character, Character> of(final char[] array) {
        return FlowStart.of(array);
    }

}
