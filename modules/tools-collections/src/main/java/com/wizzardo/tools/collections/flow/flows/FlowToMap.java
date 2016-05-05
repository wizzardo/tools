package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowGroup;
import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.collections.flow.Mapper;

import java.util.Map;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowToMap<K, V, B extends FlowGroup<K, ?>> extends FlowProcessor<B,Map<K,V>> {
    private final Map<K, V> groups;
    private final Mapper<? super B, V> mapper;

    public FlowToMap(Map<K, V> groups, Mapper<? super B, V> mapper) {
        this.groups = groups;
        this.mapper = mapper;
    }

    @Override
    public void process(final B b) {
        mapper.map(b);
        getLast(b).then(new FlowOnEnd<V>(new FlowProcessor<V, V>() {
            @Override
            public void process(V v) {
                groups.put(b.getKey(), v);
            }
        }));
    }

    @Override
    public Map<K, V> get() {
        return groups;
    }
}
