package com.wizzardo.tools.collections.flow;

/**
 * Created by wizzardo on 15.11.15.
 */
public interface Reducer<T> {
    T reduce(T a, T b);
}
