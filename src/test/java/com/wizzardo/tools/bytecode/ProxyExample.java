package com.wizzardo.tools.bytecode;


public class ProxyExample implements DynamicProxy {

    private static Handler handler;

    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public boolean[] testBooleans() {
        return (boolean[]) handler.invoke(this, "testBooleans", new Object[0]);
    }
    public Object[] testObjects() {
        return (Object[]) handler.invoke(this, "testObjects", new Object[0]);
    }
}
