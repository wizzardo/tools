package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;

import java.util.Collection;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowCollect<A, C extends Collection<A>> extends FlowProcessor<A, C> {
    private final C collection;

    public FlowCollect(C collection) {
        this.collection = collection;
    }

    @Override
    public void process(A a) {
        collection.add(a);
    }

    @Override
    public C get() {
        return collection;
    }
}
