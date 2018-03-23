package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.interfaces.Filter;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowSkipWhile<T> extends FlowProcessor<T, T> {
    protected final Filter<? super T> skip;
    protected boolean processing;

    public FlowSkipWhile(Filter<? super T> skip) {
        this.skip = skip;
    }

    @Override
    public void process(T t) {
        if (processing || (processing = !skip.allow(t)))
            child.process(t);
    }
}
