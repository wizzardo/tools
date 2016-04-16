package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowFinish<A, B> extends Flow<A, B> {

    public B startAndGet() {
        start();
        return get();
    }
}
