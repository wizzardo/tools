package com.wizzardo.tools.collections.flow.flows;


import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.collections.flow.Supplier;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowOr<A> extends FlowProcessOnEnd<A, A> {
    protected static final Supplier<?> NULL_SUPPLIER = new Supplier<Object>() {
        @Override
        public Object supply() {
            return null;
        }
    };

    protected Supplier<A> supplier;

    protected boolean processed = false;

    public FlowOr(A def) {
        result = def;
        supplier = (Supplier<A>) NULL_SUPPLIER;
    }

    public FlowOr(Supplier<A> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void process(A a) {
        processed = true;
        FlowProcessor<A, ?> child = this.child;
        if (child != null)
            child.process(a);
    }

    @Override
    protected void onEnd() {
        if (!processed) {
            super.onEnd();
        } else {
            FlowProcessor<A, ?> child = this.child;
            if (child != null)
                onEnd(child);
        }
    }

    protected A result() {
        A a = this.result;
        return a != null ? a : supplier.supply();
    }

    @Override
    public A get() {
        if (parent instanceof FlowProcessOnEnd) {
            A o = ((FlowProcessOnEnd<?, A>) parent).get();
            if (o != null) {
                return o;
            } else {
                o = result;
                if (o != null) {
                    return o;
                } else {
                    return supplier.supply();
                }
            }
        }

        throw new IllegalStateException("Parent of Or-flow cannot be cast to " + FlowProcessOnEnd.class.getSimpleName());
    }
}
