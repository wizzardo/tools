package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Supplier;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMin<A> extends FlowProcessOnEnd<A, A> {

    public FlowMin(A def) {
        result = def;
    }

    public FlowMin(Supplier<A> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void process(A a) {
        if (result == null || ((Comparable<A>) result).compareTo(a) > 0)
            result = a;
    }
}
