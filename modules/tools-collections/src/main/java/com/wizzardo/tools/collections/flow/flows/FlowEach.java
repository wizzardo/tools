package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Consumer;
import com.wizzardo.tools.collections.flow.Flow;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowEach<T> extends Flow<T, T> {
    private final Consumer<? super T> consumer;

    public FlowEach(Consumer<? super T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void process(T t) {
        consumer.consume(t);

        Flow<T, ?> child = this.child;
        if (child != null)
            child.process(t);
    }
}
