package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.interfaces.Filter;
import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowFilter<T> extends FlowProcessor<T, T> {
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
