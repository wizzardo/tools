package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;

public abstract class FlowStoppable<A, B> extends FlowProcessOnEnd<A, B> {
    protected boolean stopped;

    @Override
    protected void onEnd() {
        if (!stopped) {
            super.onEnd();
        } else {
            FlowProcessor child = this.child;
            if (child != null)
                onEnd(child);
        }
    }

    @Override
    protected void stop() {
        stopped = true;
        super.stop();
    }

    protected void stop(B b) {
        FlowProcessor<B, ?> child = this.child;
        if (child != null) {
            child.process(b);
        }
        stop();
    }
}
