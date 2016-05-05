package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.collections.flow.Mapper;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMap<A, B> extends FlowProcessor<A, B> {
    private final Mapper<? super A, B> mapper;

    public FlowMap(Mapper<? super A, B> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void process(A a) {
        B b = mapper.map(a);
        if (b != null)
            child.process(b);
    }
}
