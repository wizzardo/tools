package com.wizzardo.tools.http;

import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.FieldReflectionFactory;

import java.net.HttpURLConnection;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public interface ConnectionMethod {

    String name();

    boolean withBody();

    enum HTTPMethod implements ConnectionMethod {
        GET(),
        POST(true),
        PUT(true),
        DELETE(),
        HEAD(),
        TRACE(),
        OPTIONS(),
        CONNECT(),
        PATCH(true);

        final boolean withBody;

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

        HTTPMethod(boolean withBody) {
            this.withBody = withBody;
        }

        HTTPMethod() {
            this(false);
        }

        @Override
        public boolean withBody() {
            return withBody;
        }
    }
}