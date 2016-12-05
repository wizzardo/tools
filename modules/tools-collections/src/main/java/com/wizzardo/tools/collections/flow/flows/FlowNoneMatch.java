package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.interfaces.Filter;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowNoneMatch<T> extends FlowStoppable<T, Boolean> {
    final Filter<T> filter;

    public FlowNoneMatch(Filter<T> filter) {
        this.filter = filter;
        result = Boolean.TRUE;
    }

    @Override
    public void process(T t) {
        if (filter.allow(t)) {
            stop(result = Boolean.FALSE);
        }
    }
}
