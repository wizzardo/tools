package com.wizzardo.tools.collections.flow.flows;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowFirst<A> extends FlowFinish<A, A> {
    private A first;

    public FlowFirst(A def) {
        first = def;
    }

    @Override
    public void process(A a) {
        first = a;
        stop();
        onEnd();
    }

    @Override
    public A get() {
        return first;
    }
}
