package com.wizzardo.tools.collections.flow.flows;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMin<A> extends FlowFinish<A, A> {
    private A min;

    public FlowMin(A def) {
        min = def;
    }

    @Override
    public void process(A a) {
        if (min == null || ((Comparable<A>) min).compareTo(a) > 0)
            min = a;
    }

    @Override
    public A get() {
        return min;
    }
}
