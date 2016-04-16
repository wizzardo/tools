package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowContinue<T> extends Flow<T, T> {
    protected final Flow<?, ?> flow;

    public FlowContinue(Flow<?, ?> flow) {
        this.flow = flow;
    }

    @Override
    protected void start() {
        start(flow);
    }

    @Override
    protected void stop() {
        stop(flow);
    }

    @Override
    public void process(T t) {
        child.process(t);
    }
}
