package com.wizzardo.tools.collections.flow.flows;

import java.util.Comparator;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMinWithComparator<A> extends FlowFinish<A, A> {
    private A min;
    private final Comparator<? super A> comparator;

    public FlowMinWithComparator(A def, Comparator<? super A> comparator) {
        this.comparator = comparator;
        min = def;
    }

    @Override
    public void process(A a) {
        if (min == null || comparator.compare(min, a) > 0)
            min = a;
    }

    @Override
    public A get() {
        return min;
    }
}
