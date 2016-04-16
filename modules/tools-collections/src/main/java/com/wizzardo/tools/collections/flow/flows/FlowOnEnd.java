package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowOnEnd<T> extends Flow<T, T> {
    protected final Flow<T, ?> flow;

    public FlowOnEnd(Flow<T, ?> flow) {
        this.flow = flow;
    }

    @Override
    protected void onEnd() {
        flow.process(parent.get());
    }
}
