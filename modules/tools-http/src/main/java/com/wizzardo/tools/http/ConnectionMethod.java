package com.wizzardo.tools.http;

import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.FieldReflectionFactory;

import java.net.HttpURLConnection;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public enum ConnectionMethod {

    GET,
    POST,
    PUT,
    DELETE,
    HEAD,
    TRACE,
    OPTIONS,
    CONNECT,
    PATCH;

    static {
        FieldReflectionFactory reflectionFactory = new FieldReflectionFactory();
        try {
            FieldReflection fieldReflection = reflectionFactory.create(HttpURLConnection.class, "methods", true);
            reflectionFactory.removeFinalModifier(fieldReflection.getField());
            fieldReflection.setObject(null, new String[]{
                    GET.name(), POST.name(), HEAD.name(), OPTIONS.name(), PUT.name(), DELETE.name(), TRACE.name(), CONNECT.name(), PATCH.name()
            });
        } catch (NoSuchFieldException ignored) {
        }
    }
}