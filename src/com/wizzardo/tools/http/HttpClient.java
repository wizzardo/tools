package com.wizzardo.tools.http;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Moxa
 */
public class HttpClient {

    public static Request connect(String url) {
        return new Request(url);
    }

}
