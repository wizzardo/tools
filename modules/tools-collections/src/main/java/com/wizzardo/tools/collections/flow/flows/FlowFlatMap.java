package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;
import com.wizzardo.tools.collections.flow.FlowGroup;
import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.collections.flow.Mapper;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowFlatMap<K, V, B extends FlowGroup<K, ?>> extends FlowProcessor<B, B> {
    private final Mapper<? super B, V> mapper;
    private final FlowProcessor<V, V> continueFlow;

    public FlowFlatMap(Mapper<? super B, V> mapper, FlowProcessor<V, V> continueFlow) {
        this.mapper = mapper;
        this.continueFlow = continueFlow;
    }

    @Override
    public void process(B b) {
        mapper.map(b);
        b.getLast().then(new FlowOnEnd<V>(continueFlow));
    }

    @Override
    protected void onEnd() {
        onEnd(continueFlow);
    }

    @Override
    public Flow getLast() {
        return continueFlow.getLast();
    }
}
