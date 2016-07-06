package com.wizzardo.tools.collections.flow.flows;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowFirst<A> extends FlowStoppable<A, A> {

    public FlowFirst(A def) {
        result = def;
    }

    @Override
    public void process(A a) {
        stop(result = a);
    }
}
