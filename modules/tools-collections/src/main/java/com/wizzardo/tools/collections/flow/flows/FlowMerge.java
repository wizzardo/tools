package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;
import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMerge<B extends Flow<T>, T> extends FlowProcessor<B, T> {
    final FlowProcessor<T, T> proxy = new FlowProcessor<T, T>() {
        @Override
        public void process(T t) {
            FlowMerge.this.child.process(t);
        }

        @Override
        protected void onEnd() {
        }
    };

    @Override
    public void process(B b) {
        setChildTo(b, proxy);
    }
}
