package com.wizzardo.tools.misc.pool;

import com.wizzardo.tools.interfaces.Mapper;

/**
 * Created by wizzardo on 18.06.15.
 */
public interface Pool<T> {

    T get();

    Holder<T> holder();

    T create();

    void release(T t);

    void release(Holder<T> t);

    <R> R provide(Mapper<T, R> mapper);

    /**
     * @return current queue size
    **/
    int size();

    T reset(T t);
}
