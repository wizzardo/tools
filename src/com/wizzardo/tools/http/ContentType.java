package com.wizzardo.tools.http;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public enum ContentType {
    BINARY("application/octet-stream"),
    JSON("application/json"),
    XML("application/xml"),
    JPG("image/jpeg"),
    PNG("image/png"),
    GIF("image/gif"),;

    public final String text;

    ContentType(String text) {
        this.text = text;
    }
}
