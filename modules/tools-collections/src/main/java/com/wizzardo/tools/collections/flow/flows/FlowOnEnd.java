package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowOnEnd<T> extends FlowProcessor<T, T> {
    protected final FlowProcessor<T, ?> flow;

    public FlowOnEnd(FlowProcessor<T, ?> flow) {
        this.flow = flow;
    }

    @Override
    protected void onEnd() {
        flow.process(parent.get());
    }

    @Override
    public void process(T t) {
    }
}
