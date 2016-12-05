package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.interfaces.Filter;
import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowUntil<T> extends FlowProcessor<T, T> {
    private final Filter<? super T> until;

    public FlowUntil(Filter<? super T> until) {
        this.until = until;
    }

    @Override
    public void process(T t) {
        if (!until.allow(t))
            child.process(t);
        else
            stop();
    }
}
