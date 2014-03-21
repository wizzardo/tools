package com.wizzardo.tools.http;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public enum ContentType {
    BINARY("application/octet-stream"),
    JSON("application/json; charset=utf-8"),
    XML("text/xml; charset=utf-8"),;

    public final String text;

    ContentType(String text) {
        this.text = text;
    }
}