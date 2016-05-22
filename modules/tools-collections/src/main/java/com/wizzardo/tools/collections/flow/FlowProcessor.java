package com.wizzardo.tools.collections.flow;

/**
 * Created by wizzardo on 05/05/16.
 */
public abstract class FlowProcessor<A, B> extends Flow<B> {

    protected Flow<A> parent;

    public abstract void process(A a);

    protected void start() {
        parent.start();
    }

    protected void stop() {
        parent.stop();
    }
}
