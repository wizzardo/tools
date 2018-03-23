package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.interfaces.Filter;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowSkipUntil<T> extends FlowProcessor<T, T> {
    protected final Filter<? super T> whilst;
    protected boolean processing;

    public FlowSkipUntil(Filter<? super T> whilst) {
        this.whilst = whilst;
    }

    @Override
    public void process(T t) {
        if (processing || (processing = whilst.allow(t)))
            child.process(t);
    }
}
