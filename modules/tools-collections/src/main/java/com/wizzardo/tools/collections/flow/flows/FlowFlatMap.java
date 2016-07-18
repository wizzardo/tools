package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;
import com.wizzardo.tools.collections.flow.FlowGroup;
import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.collections.flow.Mapper;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowFlatMap<K, V extends Flow<Z>, B extends FlowGroup<K, ?>, Z> extends FlowProcessor<B, B> {
    private final Mapper<? super B, ? extends V> mapper;
    private final FlowContinue<Z> continueFlow;

    public FlowFlatMap(Mapper<? super B, ? extends V> mapper, FlowContinue<Z> continueFlow) {
        this.mapper = mapper;
        this.continueFlow = continueFlow;
    }

    @Override
    public void process(B b) {
        mapper.map(b).then(continueFlow);
    }

    @Override
    protected void onEnd() {
        continueFlow.doOnEnd();
    }

    @Override
    public Flow getLast() {
        return continueFlow.getLast();
    }
}
