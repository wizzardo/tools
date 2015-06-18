package com.wizzardo.tools.misc.pool;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by wizzardo on 18.06.15.
 */
public abstract class ThreadLocalPool<T> extends AbstractQueuedPool<T> {
    protected ThreadLocal<Queue<Holder<T>>> queue = new ThreadLocal<Queue<Holder<T>>>() {
        @Override
        protected Queue<Holder<T>> initialValue() {
            return new LinkedList<Holder<T>>();
        }
    };

    @Override
    protected Queue<Holder<T>> queue() {
        return queue.get();
    }
}
