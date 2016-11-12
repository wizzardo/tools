package com.wizzardo.tools.collections.flow.flows;


import com.wizzardo.tools.collections.flow.Supplier;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowOr<A> extends FlowProcessOnEnd<A, A> {

    protected boolean processed = false;

    public FlowOr(A def) {
        result = def;
    }

    public FlowOr(Supplier<A> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void process(A a) {
        processed = true;
        child.process(a);
    }

    @Override
    protected void onEnd() {
        if (!processed)
            super.onEnd();
        else
            onEnd(child);
    }

    @Override
    public A get() {
        throw new IllegalStateException("Should not be called directly on " + FlowOr.class.getSimpleName());
    }
}
