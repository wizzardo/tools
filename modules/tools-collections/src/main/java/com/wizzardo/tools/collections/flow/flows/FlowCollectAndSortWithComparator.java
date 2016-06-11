package com.wizzardo.tools.collections.flow.flows;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowCollectAndSortWithComparator<A, C extends List<A>> extends FlowCollect<A, C> {
    protected Comparator<? super A> comparator;

    public FlowCollectAndSortWithComparator(C collection, Comparator<? super A> comparator) {
        super(collection);
        this.comparator = comparator;
    }

    @Override
    protected void onEnd() {
        Collections.sort(result, comparator);
        super.onEnd();
    }
}
