package com.wizzardo.tools.bytecode;

public interface Handler<T> {

    CallSuper CALL_SUPER = new CallSuper();
    Object[] EMPTY_ARGS = new Object[0];

    Object invoke(T that, String method, Object[] args);

    class CallSuper {
        private CallSuper() {
        }
    }
}
