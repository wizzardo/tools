package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Supplier;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowFirst<A> extends FlowStoppable<A, A> {

    public FlowFirst(A def) {
        result = def;
    }

    public FlowFirst(Supplier<A> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void process(A a) {
        stop(result = a);
    }
}
