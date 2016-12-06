package com.wizzardo.tools.misc.pool;

/**
 * Created by wizzardo on 18.06.15.
 */
public interface Pool<T> {

    T get();

    Holder<T> holder();

    T create();

    void release(T t);

    void release(Holder<T> t);

    <R> R provide(UnsafeMapper<T, R> mapper);

    interface UnsafeMapper<A, B> {
        B map(A a) throws Exception;
    }

    /**
     * @return current queue size
    **/
    int size();

    T reset(T t);
}
