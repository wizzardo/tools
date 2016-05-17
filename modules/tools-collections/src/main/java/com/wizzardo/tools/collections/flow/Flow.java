package com.wizzardo.tools.collections.flow;

import com.wizzardo.tools.collections.flow.flows.*;

import java.util.*;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Flow<B> {
    protected FlowProcessor<B, ?> child;

    public <T extends FlowProcessor<B, C>, C> T then(T command) {
        command.parent = this;
        this.child = command;
        return command;
    }

    public Flow<B> execute() {
        start();
        return this;
    }

    protected void start() {
    }

    protected void onEnd() {
        if (child != null)
            child.onEnd();
    }

    protected void stop() {
    }

    public B get() {
        return first();
    }


    protected static void start(Flow flow) {
        flow.start();
    }

    protected static void setChildTo(Flow parent, FlowProcessor child) {
        parent.child = child;
    }

    protected static void stop(Flow flow) {
        flow.stop();
    }

    protected static void onEnd(Flow group) {
        group.onEnd();
    }

    protected static Flow getLast(Flow flow) {
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
        return reduceAnd(def, reducer).execute().get();
    }

    public Flow<B> reduceAnd(Reducer<B> reducer) {
        return reduceAnd(null, reducer);
    }

    public Flow<B> reduceAnd(B def, Reducer<B> reducer) {
        return then(new FlowReduce<B>(def, reducer));
    }

    public <T> Flow<T> merge(Mapper<? super B, ? extends Flow<? extends T>> mapper) {
        return then(new FlowMapMerge<B, T>(mapper));
    }

    public Flow<B> filter(Filter<? super B> filter) {
        return then(new FlowFilter<B>(filter));
    }

    public Flow<B> skip(int number) {
        return then(new FlowSkip<B>(number));
    }

    public Flow<B> limit(int number) {
        return then(new FlowLimit<B>(number));
    }

    public Flow<B> each(Consumer<? super B> consumer) {
        return then(new FlowEach<B>(consumer));
    }

    public Flow<B> each(ConsumerWithInt<? super B> consumer) {
        return then(new FlowEachWithIndex<B>(consumer));
    }

    /**
     * converts B into T with Mapper, ignores null-values
     */
    public <T> Flow<T> map(Mapper<? super B, T> mapper) {
        return then(new FlowMap<B, T>(mapper));
    }

    public <K> FlowGrouping<K, B, B, FlowGroup<K, B>> groupBy(final Mapper<? super B, K> toKey) {
        return groupBy(toKey, Flow.<K, FlowGroup<K, B>>hashMapSupplier());
    }

    public <K> FlowGrouping<K, B, B, FlowGroup<K, B>> groupBy(final Mapper<? super B, K> toKey, final Supplier<Map<K, FlowGroup<K, B>>> groupMapSupplier) {
        return this.then(new FlowGroupBy<K, B>(groupMapSupplier, toKey));
    }

    public <C> Flow<C> collect(C collector, BiConsumer<? super C, ? super B> accumulator) {
        return then(new FlowCollectWithAccumulator<C, B>(collector, accumulator));
    }

    public int count() {
        return countAnd().execute().get();
    }

    public Flow<Integer> countAnd() {
        return then(new FlowCount<B>());
    }

    public B first() {
        return first(null);
    }

    public B first(B def) {
        return firstAnd(def).execute().get();
    }

    public Flow<B> firstAnd() {
        return firstAnd(null);
    }

    public Flow<B> firstAnd(B def) {
        return then(new FlowFirst<B>(def));
    }

    public B last() {
        return last(null);
    }

    public B last(B def) {
        return lastAnd(def).execute().get();
    }

    public Flow<B> lastAnd() {
        return lastAnd(null);
    }

    public Flow<B> lastAnd(B def) {
        return then(new FlowLast<B>(def));
    }

    public B min(Comparator<? super B> comparator) {
        return min(null, comparator);
    }

    public B min(B def, Comparator<? super B> comparator) {
        return minAnd(def, comparator).execute().get();
    }

    public B min() {
        return min((B) null);
    }

    public B min(B def) {
        return minAnd(def).execute().get();
    }

    public Flow<B> minAnd(Comparator<? super B> comparator) {
        return minAnd(null, comparator);
    }

    public Flow<B> minAnd(B def, Comparator<? super B> comparator) {
        return then(new FlowMinWithComparator<B>(def, comparator));
    }

    public Flow<B> minAnd() {
        return minAnd((B) null);
    }

    public Flow<B> minAnd(B def) {
        return then(new FlowMin<B>(def));
    }

    public B max(Comparator<? super B> comparator) {
        return max(null, comparator);
    }

    public B max(B def, Comparator<? super B> comparator) {
        return maxAnd(def, comparator).execute().get();
    }

    public B max() {
        return max((B) null);
    }

    public B max(B def) {
        return maxAnd(def).execute().get();
    }

    public Flow<B> maxAnd(Comparator<? super B> comparator) {
        return maxAnd(null, comparator);
    }

    public Flow<B> maxAnd(B def, Comparator<? super B> comparator) {
        return then(new FlowMaxWithComparator<B>(def, comparator));
    }

    public Flow<B> maxAnd() {
        return maxAnd((B) null);
    }

    public Flow<B> maxAnd(B def) {
        return then(new FlowMax<B>(def));
    }

    public <T> Flow<T> async(Mapper<B, Flow<T>> mapper) {
        return then(new FlowAsync<B, T>(mapper));
    }

    public boolean any(Filter<B> filter) {
        return anyAnd(filter).execute().get();
    }

    public Flow<Boolean> anyAnd(Filter<B> filter) {
        return then(new FlowAnyMatch<B>(filter));
    }

    public boolean all(Filter<B> filter) {
        return allAnd(filter).execute().get();
    }

    public Flow<Boolean> allAnd(Filter<B> filter) {
        return then(new FlowAllMatch<B>(filter));
    }

    public boolean none(Filter<B> filter) {
        return noneAnd(filter).execute().get();
    }

    public Flow<Boolean> noneAnd(Filter<B> filter) {
        return then(new FlowNoneMatch<B>(filter));
    }

    public List<B> toList() {
        return toListAnd().execute().get();
    }

    public Flow<ArrayList<B>> toListAnd() {
        return collectAnd(new ArrayList<B>());
    }

    public <C extends Collection<B>> C collect(C collection) {
        return then(new FlowCollect<B, C>(collection)).execute().get();
    }

    public <C extends Collection<B>> Flow<C> collectAnd(C collection) {
        return then(new FlowCollect<B, C>(collection));
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


    public static <T> Flow<T> of(final Iterable<T> iterable) {
        return FlowStart.of(iterable);
    }

    public static <T> Flow<T> of(final Iterator<T> iterator) {
        return FlowStart.of(iterator);
    }

    public static <K, V> Flow<Map.Entry<K, V>> of(Map<K, V> map) {
        return FlowStart.of(map.entrySet());
    }

    public static <T> Flow<T> of(final T... array) {
        return FlowStart.of(array);
    }

    public static <T> Flow<T> of(Supplier<T> supplier) {
        return FlowStart.of(supplier);
    }

    public static Flow<Integer> of(final int[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Long> of(final long[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Double> of(final double[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Float> of(final float[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Byte> of(final byte[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Boolean> of(final boolean[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Short> of(final short[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Character> of(final char[] array) {
        return FlowStart.of(array);
    }

}
