package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.interfaces.Consumer;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowEach<T> extends FlowProcessor<T, T> {
    private final Consumer<? super T> consumer;

    public FlowEach(Consumer<? super T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void process(T t) {
        consumer.consume(t);

        FlowProcessor<T, ?> child = this.child;
        if (child != null)
            child.process(t);
    }
}
