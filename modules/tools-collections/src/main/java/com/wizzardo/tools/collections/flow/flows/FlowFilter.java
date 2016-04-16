package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Filter;
import com.wizzardo.tools.collections.flow.Flow;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowFilter<T> extends Flow<T, T> {
    private final Filter<? super T> filter;

    public FlowFilter(Filter<? super T> filter) {
        this.filter = filter;
    }

    @Override
    public void process(T t) {
        if (filter.allow(t))
            child.process(t);
    }
}
