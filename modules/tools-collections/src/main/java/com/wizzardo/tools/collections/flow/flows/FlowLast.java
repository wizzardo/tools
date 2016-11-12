package com.wizzardo.tools.collections.flow.flows;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowLast<A> extends FlowProcessOnEnd<A, A> {
    public FlowLast(A def) {
        result = def;
    }

    @Override
    public void process(A a) {
        result = a;
    }
}
