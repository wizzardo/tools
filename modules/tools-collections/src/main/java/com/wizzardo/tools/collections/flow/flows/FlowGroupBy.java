package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowGroup;
import com.wizzardo.tools.collections.flow.FlowGrouping;
import com.wizzardo.tools.collections.flow.Mapper;
import com.wizzardo.tools.collections.flow.Supplier;

import java.util.Map;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowGroupBy<K, B> extends FlowGrouping<K, B, B, FlowGroup<K, B>> {
    private final Mapper<? super B, K> toKey;

    public FlowGroupBy(Supplier<Map<K, FlowGroup<K, B>>> groupMapSupplier, Mapper<? super B, K> toKey) {
        super(groupMapSupplier.supply());
        this.toKey = toKey;
    }

    @Override
    public void process(B b) {
        K key = toKey.map(b);
        FlowGroup<K, B> group = groups.get(key);
        if (group == null) {
            groups.put(key, group = new FlowGroup<K, B>(key));
            child.process(group);
        }
        group.process(b);
    }

    @Override
    protected void onEnd() {
        for (FlowGroup<K, B> group : groups.values()) {
            onEnd(group);
        }
        super.onEnd();
    }
}
