package com.wizzardo.tools.collections.flow.flows;

import java.util.Comparator;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMaxWithComparator<A> extends FlowProcessOnEnd<A, A> {
    private final Comparator<? super A> comparator;

    public FlowMaxWithComparator(A def, Comparator<? super A> comparator) {
        this.comparator = comparator;
        result = def;
    }

    @Override
    public void process(A a) {
        if (result == null || comparator.compare(result, a) < 0)
            result = a;
    }
}
