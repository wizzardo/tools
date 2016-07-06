package com.wizzardo.tools.collections.flow;

import com.wizzardo.tools.collections.flow.flows.FlowNoop;

/**
 * Created by wizzardo on 08.11.15.
 */
public class FlowGroup<K, T> extends FlowProcessor<T, T> {
    public final K key;
    private boolean stopped;

    public FlowGroup(K key) {
        this.key = key;
        child = new FlowNoop<T>();
    }

    public K getKey() {
        return key;
    }

    @Override
    public void process(T t) {
        if (stopped)
            return;

        child.process(t);
    }

    @Override
    protected void start() {
    }

    @Override
    protected void onEnd() {
        child.onEnd();
    }

    @Override
    protected void stop() {
        stopped = true;
    }
}
