package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.ConsumerWithInt;
import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowEachWithIndex<T> extends FlowProcessor<T, T> {
    private final ConsumerWithInt<? super T> consumer;
    private int index = 0;

    public FlowEachWithIndex(ConsumerWithInt<? super T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void process(T t) {
        consumer.consume(index++, t);

        FlowProcessor<T, ?> child = this.child;
        if (child != null)
            child.process(t);
    }
}
