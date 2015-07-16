package com.wizzardo.tools.cache;

/**
 * @author: wizzardo
 * Date: 2/12/14
 */
public interface Computable<K, V> {

    V compute(K k) throws Exception;
}
