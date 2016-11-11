package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.collections.flow.Supplier;

/**
 * Created by wizzardo on 11/05/16.
 */
public abstract class FlowProcessOnEnd<A, B> extends FlowProcessor<A, B> {
    protected static final Supplier<?> NULL_SUPPLIER = new Supplier<Object>() {
        @Override
        public Object supply() {
            return null;
        }
    };

    protected boolean started = false;
    protected B result;
    protected Supplier<B> supplier = (Supplier<B>) NULL_SUPPLIER;

    public FlowProcessOnEnd() {
    }

    public FlowProcessOnEnd(B def) {
        result = def;
    }

    public FlowProcessOnEnd(Supplier<B> defaultSupplier) {
        this.supplier = defaultSupplier;
    }

    @Override
    protected void onEnd() {
        FlowProcessor<B, ?> child = this.child;
        if (child != null) {
            child.process(result());
            onEnd(child);
        }
    }

    @Override
    protected void start() {
        if (!started) {
            started = true;
            super.start();
        }
    }

    public B get() {
        start();
        return result();
    }

    protected B result() {
        B b = this.result;
        return b != null ? b : supplier.supply();
    }
}
