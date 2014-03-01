package com.wizzardo.tools.http;

import java.util.Date;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public class Cookie {
    String key;
    String value;
    String path;
    String domain;
    Date expired;

    public Cookie(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        //Set-Cookie: RMID=732423sdfs73242; expires=Fri, 31 Dec 2010 23:59:59 GMT; path=/; domain=.example.net
        return key + "=" + value + "; expires=" + expired + "; path=" + path + "; domain=" + domain;
    }
}