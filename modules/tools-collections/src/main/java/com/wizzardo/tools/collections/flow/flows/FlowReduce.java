package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Reducer;
import com.wizzardo.tools.collections.flow.Supplier;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowReduce<T> extends FlowProcessOnEnd<T, T> {
    private final Reducer<T> reducer;

    public FlowReduce(T def, Reducer<T> reducer) {
        this.reducer = reducer;
        result = def;
    }

    public FlowReduce(Supplier<T> supplier, Reducer<T> reducer) {
        this.reducer = reducer;
        this.supplier = supplier;
    }

    @Override
    public void process(T t) {
        T prev = this.result;
        if (prev == null)
            this.result = t;
        else
            this.result = reducer.reduce(prev, t);
    }
}
