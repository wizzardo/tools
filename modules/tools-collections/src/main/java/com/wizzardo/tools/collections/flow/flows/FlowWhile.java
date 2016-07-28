package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Filter;
import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowWhile<T> extends FlowProcessor<T, T> {
    private final Filter<? super T> whilst;

    public FlowWhile(Filter<? super T> whilst) {
        this.whilst = whilst;
    }

    @Override
    public void process(T t) {
        if (whilst.allow(t))
            child.process(t);
        else
            stop();
    }
}
