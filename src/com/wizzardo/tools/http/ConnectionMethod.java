package com.wizzardo.tools.http;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public enum ConnectionMethod {

    GET("GET"), POST("POST");
    private String method;

    private ConnectionMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return method;
    }
}