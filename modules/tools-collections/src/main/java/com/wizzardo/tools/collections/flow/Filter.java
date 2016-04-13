package com.wizzardo.tools.collections.flow;

/**
 * Created by wizzardo on 15.11.15.
 */
public interface Filter<T> {
    boolean allow(T t);
}
