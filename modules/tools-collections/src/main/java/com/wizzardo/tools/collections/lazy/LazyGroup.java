package com.wizzardo.tools.collections.lazy;

/**
 * Created by wizzardo on 08.11.15.
 */
public class LazyGroup<K, A, B> extends Lazy<A, B> {
    private K key;

    LazyGroup(K key, Command<A, B> command) {
        super(command);
        this.key = key;
    }

    public K getKey() {
        return key;
    }
}
