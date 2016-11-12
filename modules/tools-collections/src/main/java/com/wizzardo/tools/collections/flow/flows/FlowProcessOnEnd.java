package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 11/05/16.
 */
public abstract class FlowProcessOnEnd<A, B> extends FlowProcessor<A, B> {

    protected boolean started = false;
    protected B result;

    public FlowProcessOnEnd() {
    }

    public FlowProcessOnEnd(B def) {
        result = def;
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
        return result;
    }
}
