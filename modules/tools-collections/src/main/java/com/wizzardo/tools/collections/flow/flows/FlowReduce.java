package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Reducer;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowReduce<T> extends FlowProcessOnEnd<T, T> {
    private final Reducer<T> reducer;
    private T prev;

    public FlowReduce(T def, Reducer<T> reducer) {
        this.reducer = reducer;
        prev = def;
    }

    @Override
    public void process(T t) {
        T prev = this.prev;
        if (prev == null)
            this.prev = t;
        else
            this.prev = reducer.reduce(prev, t);
    }

    @Override
    public T get() {
        return prev;
    }
}
