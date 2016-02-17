package com.wizzardo.tools.collections.lazy;

/**
 * Created by wizzardo on 15.11.15.
 */
public interface Iterater<A, B> {
    void iterate(A a, Consumer<B> consumer);
}
