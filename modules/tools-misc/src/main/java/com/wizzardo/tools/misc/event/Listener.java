package com.wizzardo.tools.misc.event;

public interface Listener<E, D> {
    void on(E event, D data) throws Exception;
}
