package com.wizzardo.tools.misc;

/**
 * Created by wizzardo on 05.12.15.
 */
public class With {

    public static <T> T with(T t, Consumer<T> consumer) {
        consumer.consume(t);
        return t;
    }

    public static <A, B> B map(A a, Mapper<? super A, B> mapper) {
        return mapper.map(a);
    }

    public static <T> T cast(Object o) {
        return (T) o;
    }
}
