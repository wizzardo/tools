package com.wizzardo.tools.bytecode;

public interface DynamicProxy {
    boolean  SUPPORTED = DynamicProxyFactory.isSupported();

    void setHandler(Handler handler);

    static <T> T create(Class<T> clazz, Handler<T> handler) {
        T t = DynamicProxyFactory.create(clazz);
        ((DynamicProxy) t).setHandler(handler);
        return t;
    }
}
