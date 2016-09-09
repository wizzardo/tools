package com.wizzardo.tools.cache;

/**
 * Created by wizzardo on 09/09/16.
 */
public interface CacheListener<K, V> {
    void onEvent(K k, V v);
}
