package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Reducer;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowReduce<T> extends FlowFinish<T, T> {
    private final Reducer<T> reducer;
    private T prev;

    public FlowReduce(T def, Reducer<T> reducer) {
        this.reducer = reducer;
        prev = def;
    }

    @Override
    public void process(T t) {
        if (prev == null)
            prev = t;
        else
            prev = reducer.reduce(prev, t);
    }

    @Override
    public T get() {
        return prev;
    }
}