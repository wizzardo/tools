package com.wizzardo.tools.collections.flow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wizzardo on 08.11.15.
 */
public class FlowGrouping<K, T, A, B extends FlowGroup<K, T>> extends Flow<A, B> {

    protected final Map<K, FlowGroup<K, T>> groups;

    FlowGrouping(Map<K, FlowGroup<K, T>> groups) {
        this.groups = groups;
    }

    public <V> Flow<V, V> flatMap(Mapper<? super B, V> mapper) {
        FlowContinue<V> continueCommand = new FlowContinue<V>(this);
        then(new GroupFlow<K, V, B>(mapper, continueCommand));
        return continueCommand;
    }

    public Map<K, List<T>> toMap() {
        return toMap(Flow.<K, T>flowGroupListMapper());
    }

    public <V> Map<K, V> toMap(Mapper<? super B, V> mapper) {
        ToMapFlow<K, V, B> toMap;
        then(toMap = new ToMapFlow<K, V, B>((Map<K, V>) groups, mapper));
        toMap.start();
        return toMap.get();
    }

    @Override
    public FlowGrouping<K, T, B, B> filter(final Filter<? super B> filter) {
        return this.then(new FlowGrouping<K, T, B, B>(new LinkedHashMap<K, FlowGroup<K, T>>()) {
            @Override
            protected void process(B b) {
                if (filter.allow(b)) {
                    groups.put(b.getKey(), b);
                    child.process(b);
                }
            }
        });
    }

    @Override
    public FlowGrouping<K, T, B, B> each(final Consumer<? super B> consumer) {
        return this.then(new FlowGrouping<K, T, B, B>(groups) {
            @Override
            protected void process(B b) {
                consumer.consume(b);

                Flow<B, ?> child = this.child;
                if (child != null)
                    child.process(b);
            }
        });
    }

    public FlowGrouping<K, T, B, B> each(final ConsumerWithInt<? super B> consumer) {
        return then(new FlowGrouping<K, T, B, B>(groups) {
            int index = 0;

            @Override
            protected void process(B b) {
                consumer.consume(index++, b);

                Flow<B, ?> child = this.child;
                if (child != null)
                    child.process(b);
            }
        });
    }

    public Flow<B, T> merge() {
        return then(new FlowMerge<B, T>());
    }

    public <V> Flow<B, V> merge(Mapper<? super B, ? extends Flow<V, V>> mapper) {
        return then(new FlowMapMerge<B, V>(mapper));
    }

    private static class GroupFlow<K, V, B extends FlowGroup<K, ?>> extends Flow<B, B> {
        private final Mapper<? super B, V> mapper;
        private final Flow<V, V> continueFlow;

        public GroupFlow(Mapper<? super B, V> mapper, Flow<V, V> continueFlow) {
            this.mapper = mapper;
            this.continueFlow = continueFlow;
        }

        @Override
        protected void process(B b) {
            mapper.map(b);
            getLast(b).then(new FlowOnEnd<V>(continueFlow));
        }

        @Override
        protected void onEnd() {
        }
    }

    private static class ToMapFlow<K, V, B extends FlowGroup<K, ?>> extends FinishFlow<B, Map<K, V>> {
        private Map<K, V> groups;
        private final Mapper<? super B, V> mapper;

        public ToMapFlow(Map<K, V> groups, Mapper<? super B, V> mapper) {
            this.groups = groups;
            this.mapper = mapper;
        }

        @Override
        protected void process(final B b) {
            mapper.map(b);
            getLast(b).then(new FlowOnEnd<V>(new Flow<V, V>() {
                @Override
                protected void process(V v) {
                    groups.put(b.getKey(), v);
                }
            }));
        }

        @Override
        protected Map<K, V> get() {
            return groups;
        }
    }

    private static class FlowContinue<T> extends Flow<T, T> {
        private Flow<?, ?> flow;

        FlowContinue(Flow<?, ?> flow) {
            this.flow = flow;
        }

        @Override
        protected void start() {
            flow.start();
        }

        @Override
        protected void stop() {
            flow.stop();
        }

        @Override
        protected void process(T t) {
            child.process(t);
        }
    }

    private static class FlowOnEnd<T> extends Flow<T, T> {
        private Flow<T, ?> flow;

        private FlowOnEnd(Flow<T, ?> flow) {
            this.flow = flow;
        }

        @Override
        protected void onEnd() {
            flow.process(parent.get());
        }
    }
}
