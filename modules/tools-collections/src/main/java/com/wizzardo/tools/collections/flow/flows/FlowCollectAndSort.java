package com.wizzardo.tools.collections.flow.flows;

import java.util.Collections;
import java.util.List;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowCollectAndSort<A, C extends List<A>> extends FlowCollect<A, C> {

    public FlowCollectAndSort(C collection) {
        super(collection);
    }

    @Override
    protected void onEnd() {
        Collections.sort((List<Comparable>) result);
        super.onEnd();
    }
}
