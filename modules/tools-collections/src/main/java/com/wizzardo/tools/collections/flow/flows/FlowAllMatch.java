package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Filter;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowAllMatch<T> extends FlowProcessOnEnd<T, Boolean> {
    final Filter<T> filter;
    boolean result = true;

    public FlowAllMatch(Filter<T> filter) {
        this.filter = filter;
    }

    @Override
    public void process(T t) {
        if (!filter.allow(t)) {
            result = false;
            stop();
        }
    }

    @Override
    public Boolean get() {
        return result;
    }
}
