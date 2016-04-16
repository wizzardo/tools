package com.wizzardo.tools.collections.flow.flows;

import java.util.Comparator;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMaxWithComparator<A> extends FlowFinish<A, A> {
    private A max;
    private final Comparator<? super A> comparator;

    public FlowMaxWithComparator(A def, Comparator<? super A> comparator) {
        this.comparator = comparator;
        max = def;
    }

    @Override
    public void process(A a) {
        if (max == null || comparator.compare(max, a) < 0)
            max = a;
    }

    @Override
    public A get() {
        return max;
    }
}
