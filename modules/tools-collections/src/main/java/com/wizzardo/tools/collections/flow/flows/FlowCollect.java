package com.wizzardo.tools.collections.flow.flows;

import java.util.Collection;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowCollect<A, C extends Collection<A>> extends FlowProcessOnEnd<A, C> {

    public FlowCollect(C collection) {
        result = collection;
    }

    @Override
    public void process(A a) {
        result.add(a);
    }
}
