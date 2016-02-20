package com.wizzardo.tools.misc;

/**
 * Created by wizzardo on 20.02.16.
 */
public interface Mapper<A, B> {
    B map(A a);
}
