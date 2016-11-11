package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Supplier;

import java.util.Comparator;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMinWithComparator<A> extends FlowProcessOnEnd<A, A> {
    private final Comparator<? super A> comparator;

    public FlowMinWithComparator(A def, Comparator<? super A> comparator) {
        this.comparator = comparator;
        result = def;
    }

    public FlowMinWithComparator(Supplier<A> supplier, Comparator<? super A> comparator) {
        this.comparator = comparator;
        this.supplier = supplier;
    }

    @Override
    public void process(A a) {
        if (result == null || comparator.compare(result, a) > 0)
            result = a;
    }
}
