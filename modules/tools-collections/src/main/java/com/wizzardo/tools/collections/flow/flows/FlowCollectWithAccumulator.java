package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.BiConsumer;
import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowCollectWithAccumulator<C, T> extends FlowProcessor<T, C> {
    private final BiConsumer<? super C, ? super T> accumulator;
    private final C collector;

    public FlowCollectWithAccumulator(C collector, BiConsumer<? super C, ? super T> accumulator) {
        this.accumulator = accumulator;
        this.collector = collector;
    }

    @Override
    public void process(T t) {
        accumulator.consume(collector, t);
    }

    @Override
    public C get() {
        return collector;
    }
}
