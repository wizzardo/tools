package com.wizzardo.tools.collections.flow;

/**
 * Created by wizzardo on 08.11.15.
 */
public class FlowGroup<K, T> extends Flow<T, T> {
    public final K key;

    private boolean stopped;

    FlowGroup(K key) {
        this.key = key;
        child = new NoopFlow<T>();
    }

    public K getKey() {
        return key;
    }

    @Override
    protected void process(T t) {
        if (stopped)
            return;

        processToChild(t);
    }

    @Override
    protected void start() {
    }

    @Override
    protected void onEnd() {
        if (!stopped)
            child.onEnd();
    }

    @Override
    protected void stop() {
        stopped = true;
    }
}
