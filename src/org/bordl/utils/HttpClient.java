package org.bordl.utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Moxa
 */
public class HttpClient {

    private String lastURL;

    private String createPostParameters(HashMap<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, Object>> iter = params.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    private String createURL(String url, HashMap<String, Object> params) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        Iterator<Map.Entry<String, Object>> iter = params.entrySet().iterator();
        if (iter.hasNext()) {
            sb.append("?");
        }
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue().toString(), "utf-8"));
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    public HttpURLConnection createConnection(String url) throws IOException {
        return createConnection(url, null, null, "GET", null);
    }

    public HttpURLConnection createConnection(String url, String cookie) throws IOException {
        return createConnection(url, cookie, null, "GET", null);
    }

    public HttpURLConnection createConnection(String url, HashMap<String, Object> params) throws IOException {
        return createConnection(url, null, params, "GET", null);
    }

    public HttpURLConnection createConnection(String url, String cookie, HashMap<String, Object> params) throws IOException {
        return createConnection(url, cookie, params, "GET", null);
    }

    public HttpURLConnection createConnection(String url, String cookie, HashMap<String, Object> params, String method) throws IOException {
        return createConnection(url, cookie, params, method, null);
    }

    public HttpURLConnection createConnection(String url, String cookie, HashMap<String, Object> params, String method, String referer) throws IOException {
        if (method.equalsIgnoreCase("GET")) {
            url = createURL(url, params);
        }
        URL u = new URL(url);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();

        if (lastURL != null) {
            c.setRequestProperty("Referer", lastURL);
        }
        if (referer != null) {
            c.setRequestProperty("Referer", referer);
        }
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.187 Safari/535.1");
        c.setRequestProperty("Cookie", cookie);
        c.setRequestMethod(method);
        if (method.equalsIgnoreCase("POST")) {
            c.setDoOutput(true);
            c.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String data = createPostParameters(params);
            c.addRequestProperty("Content-Length", data.length() + "");
            OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream());
            out.write(data);
            out.flush();
            out.close();
        }
        lastURL = url;
        return c;
    }
}
