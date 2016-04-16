package com.wizzardo.tools.collections.flow.flows;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowLast<A> extends FlowFinish<A, A> {
    private A last;

    public FlowLast(A def) {
        last = def;
    }

    @Override
    public void process(A a) {
        last = a;
    }

    @Override
    public A get() {
        return last;
    }
}
