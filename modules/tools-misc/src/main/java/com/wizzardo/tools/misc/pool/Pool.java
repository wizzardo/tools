package com.wizzardo.tools.misc.pool;

/**
 * Created by wizzardo on 18.06.15.
 */
public interface Pool<T> {

    T get();

    Holder<T> holder();

    T create();

    void release(T t);

}
