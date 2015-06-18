package com.wizzardo.tools.misc.pool;

import java.io.Closeable;

/**
 * Created by wizzardo on 18.06.15.
 */
public interface Holder<T> extends Closeable {

    T get();
}
