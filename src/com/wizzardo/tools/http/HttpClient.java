package com.wizzardo.tools.http;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Moxa
 */
public class HttpClient {
    private static HttpSession session;

    public static Request connect(String url) {
        if (session == null)
            session = new HttpSession();

        return session.createRequest(url);
    }

}
