package com.wizzardo.tools.collections.lazy;

/**
 * Created by wizzardo on 15.11.15.
 */
public interface Mapper<A, B> {
    B map(A a);
}
