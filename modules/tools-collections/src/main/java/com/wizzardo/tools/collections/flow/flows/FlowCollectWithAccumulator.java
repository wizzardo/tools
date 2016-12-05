package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.interfaces.BiConsumer;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowCollectWithAccumulator<C, T> extends FlowProcessOnEnd<T, C> {
    private final BiConsumer<? super C, ? super T> accumulator;

    public FlowCollectWithAccumulator(C collector, BiConsumer<? super C, ? super T> accumulator) {
        this.accumulator = accumulator;
        this.result = collector;
    }

    @Override
    public void process(T t) {
        accumulator.consume(result, t);
    }
}
