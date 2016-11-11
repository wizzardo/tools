package com.wizzardo.tools.collections.flow.flows;


import com.wizzardo.tools.collections.flow.Supplier;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowLast<A> extends FlowProcessOnEnd<A, A> {
    public FlowLast(A def) {
        result = def;
    }

    public FlowLast(Supplier<A> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void process(A a) {
        result = a;
    }
}
