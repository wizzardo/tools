package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowCount<A> extends FlowProcessor<A, Integer> {
    private int count = 0;

    @Override
    public void process(A a) {
        count++;
    }

    @Override
    public Integer get() {
        return count;
    }

    public int getCount() {
        return count;
    }
}
