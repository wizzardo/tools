package com.wizzardo.tools.misc.pool;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by wizzardo on 18.06.15.
 */
public abstract class SharedPool<T> extends AbstractQueuedPool<T> {

    protected final Queue<Holder<T>> queue = new ConcurrentLinkedQueue<Holder<T>>();

    @Override
    protected Queue<Holder<T>> queue() {
        return queue;
    }
}
