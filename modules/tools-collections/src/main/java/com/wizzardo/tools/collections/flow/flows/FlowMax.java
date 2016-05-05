package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMax<A> extends FlowProcessor<A, A> {
    private A max;

    public FlowMax(A def) {
        max = def;
    }

    @Override
    public void process(A a) {
        if (max == null || ((Comparable<A>) max).compareTo(a) < 0)
            max = a;
    }

    @Override
    public A get() {
        return max;
    }
}
