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

    <R> R provide(Consumer<T, R> consumer);

    interface Consumer<T, R> {
        R consume(T t) throws Exception;
    }

    /**
     * @return current queue size
    **/
    int size();
}
