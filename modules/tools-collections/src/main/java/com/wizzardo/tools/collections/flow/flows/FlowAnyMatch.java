package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Filter;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowAnyMatch<T> extends FlowProcessOnEnd<T, Boolean> {
    final Filter<T> filter;

    public FlowAnyMatch(Filter<T> filter) {
        this.filter = filter;
        result = Boolean.FALSE;
    }

    @Override
    public void process(T t) {
        if (filter.allow(t)) {
            result = true;
            stop();
        }
    }
}
