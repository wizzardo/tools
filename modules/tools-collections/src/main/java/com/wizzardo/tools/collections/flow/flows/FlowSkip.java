package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowSkip<T> extends FlowProcessor<T, T> {
    private final int number;
    private int counter = 0;

    public FlowSkip(int number) {
        this.number = number;
    }

    @Override
    public void process(T t) {
        if (counter >= number)
            child.process(t);
        else
            counter++;
    }
}
