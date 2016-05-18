package com.wizzardo.tools.collections.flow.flows;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMax<A> extends FlowProcessOnEnd<A, A> {

    public FlowMax(A def) {
        result = def;
    }

    @Override
    public void process(A a) {
        if (result == null || ((Comparable<A>) result).compareTo(a) < 0)
            result = a;
    }
}
