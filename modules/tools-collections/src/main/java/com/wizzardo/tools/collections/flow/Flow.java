package com.wizzardo.tools.collections.flow;

import com.wizzardo.tools.collections.flow.flows.*;

import java.util.*;
import java.util.concurrent.ExecutorService;

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
            return (List) flowGroup.toList().get();
        }
    };

    public static <K, V> Mapper<FlowGroup<K, V>, List<V>> flowGroupListMapper() {
        return FLOW_GROUP_LIST_MAPPER;
    }


    public FlowReduce<B> reduce(Reducer<B> reducer) {
        return reduce(null, reducer);
    }

    public FlowReduce<B> reduce(B def, Reducer<B> reducer) {
        return then(new FlowReduce<B>(def, reducer));
    }

    public <Z, V extends Flow<Z>> Flow<Z> flatMap(Mapper<? super B, ? extends V> mapper) {
        return then(new FlowMapMerge<B, Z>(mapper));
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

    public <C> FlowCollectWithAccumulator<C, B> collect(C collector, BiConsumer<? super C, ? super B> accumulator) {
        return then(new FlowCollectWithAccumulator<C, B>(collector, accumulator));
    }

    public FlowCount<B> count() {
        return then(new FlowCount<B>());
    }

    public FlowFirst<B> first() {
        return first(null);
    }

    public FlowFirst<B> first(B def) {
        return then(new FlowFirst<B>(def));
    }

    public FlowLast<B> last() {
        return last(null);
    }

    public FlowLast<B> last(B def) {
        return then(new FlowLast<B>(def));
    }

    public FlowMinWithComparator<B> min(Comparator<? super B> comparator) {
        return min(null, comparator);
    }

    public FlowMinWithComparator<B> min(B def, Comparator<? super B> comparator) {
        return then(new FlowMinWithComparator<B>(def, comparator));
    }

    public FlowMin<B> min() {
        return min((B) null);
    }

    public FlowMin<B> min(B def) {
        return then(new FlowMin<B>(def));
    }

    public FlowMaxWithComparator<B> max(Comparator<? super B> comparator) {
        return max(null, comparator);
    }

    public FlowMaxWithComparator<B> max(B def, Comparator<? super B> comparator) {
        return then(new FlowMaxWithComparator<B>(def, comparator));
    }

    public FlowMax<B> max() {
        return max((B) null);
    }

    public FlowMax<B> max(B def) {
        return then(new FlowMax<B>(def));
    }

    public <T> Flow<T> async(Mapper<B, Flow<T>> mapper) {
        return then(new FlowAsync<B, T>(mapper));
    }

    public <T> Flow<T> async(int queueLimit, Mapper<B, Flow<T>> mapper) {
        return then(new FlowAsync<B, T>(queueLimit, mapper));
    }

    public <T> Flow<T> async(ExecutorService service, int queueLimit, Mapper<B, Flow<T>> mapper) {
        return then(new FlowAsync<B, T>(service, queueLimit, mapper));
    }

    public FlowAnyMatch<B> any(Filter<B> filter) {
        return then(new FlowAnyMatch<B>(filter));
    }

    public FlowAllMatch<B> all(Filter<B> filter) {
        return then(new FlowAllMatch<B>(filter));
    }

    public FlowNoneMatch<B> none(Filter<B> filter) {
        return then(new FlowNoneMatch<B>(filter));
    }

    public FlowCollect<B, ArrayList<B>> toList() {
        return collect(new ArrayList<B>());
    }

    public <C extends Collection<B>> FlowCollect<B, C> collect(C collection) {
        return then(new FlowCollect<B, C>(collection));
    }

    public FlowCollectAndSortWithComparator<B, List<B>> toSortedList(Comparator<? super B> comparator) {
        return toSortedList(new ArrayList<B>(), comparator);
    }

    public FlowCollectAndSort<B, List<B>> toSortedList() {
        return toSortedList(new ArrayList<B>());
    }

    public FlowCollectAndSortWithComparator<B, List<B>> toSortedList(List<B> list, Comparator<? super B> comparator) {
        return then(new FlowCollectAndSortWithComparator<B, List<B>>(list, comparator));
    }

    public FlowCollectAndSort<B, List<B>> toSortedList(List<B> list) {
        return then(new FlowCollectAndSort<B, List<B>>(list));
    }

    public FlowJoin<B> join(String separator) {
        return join(separator, new StringBuilder());
    }

    public FlowJoin<B> join(String separator, StringBuilder sb) {
        return then(new FlowJoin<B>(sb, separator));
    }

    public <K, V> FlowToMap<K, V, FlowGroup<K, B>> toMap(Mapper<B, K> toKey, Mapper<FlowGroup<K, B>, V> toValue) {
        return toMap(Flow.<K, FlowGroup<K, B>>hashMapSupplier(), toKey, toValue);
    }

    public <K> FlowToMap<K, List<B>, FlowGroup<K, B>> toMap(Supplier<Map<K, FlowGroup<K, B>>> groupMapSupplier, Mapper<B, K> toKey) {
        return toMap(groupMapSupplier, toKey, Flow.<K, B>flowGroupListMapper());
    }

    public <K, V> FlowToMap<K, V, FlowGroup<K, B>> toMap(Supplier<Map<K, FlowGroup<K, B>>> groupMapSupplier, Mapper<B, K> toKey, Mapper<FlowGroup<K, B>, V> toValue) {
        return groupBy(toKey, groupMapSupplier).toMap(toValue);
    }


    public static <T> Flow<T> of(Iterable<T> iterable) {
        return FlowStart.of(iterable);
    }

    public static <T> Flow<T> of(Iterator<T> iterator) {
        return FlowStart.of(iterator);
    }

    public static <K, V> Flow<Map.Entry<K, V>> of(Map<K, V> map) {
        return FlowStart.of(map.entrySet());
    }

    public static <T> Flow<T> of(T... array) {
        return FlowStart.of(array);
    }

    public static <T> Flow<T> of(T t) {
        return FlowStart.of(t);
    }

    public static <T> Flow<T> of(Supplier<T> supplier) {
        return FlowStart.of(supplier);
    }

    public static <T> Flow<T> of(Supplier<T>... supplier) {
        return FlowStart.of(supplier);
    }

    public static Flow<Integer> of(int[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Long> of(long[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Double> of(double[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Float> of(float[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Byte> of(byte[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Boolean> of(boolean[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Short> of(short[] array) {
        return FlowStart.of(array);
    }

    public static Flow<Character> of(char[] array) {
        return FlowStart.of(array);
    }

}
