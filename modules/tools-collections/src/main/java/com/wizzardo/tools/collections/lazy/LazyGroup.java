package com.wizzardo.tools.collections.lazy;

/**
 * Created by wizzardo on 08.11.15.
 */
public class LazyGroup<K, T> extends Command<T, T> {
    public final K key;

    private boolean stopped;

    LazyGroup(K key) {
        this.key = key;
        child = new NoopCommand<T>();
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
