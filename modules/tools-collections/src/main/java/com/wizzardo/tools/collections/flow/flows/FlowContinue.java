package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;
import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowContinue<T> extends FlowProcessor<T, T> {
    protected final Flow<?> flow;

    public FlowContinue(Flow<?> flow) {
        this.flow = flow;
        child = new FlowNoop<T>();
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

    @Override
    protected void onEnd() {
    }

    protected void doOnEnd() {
        if (child != null)
            onEnd(child);
    }
}
