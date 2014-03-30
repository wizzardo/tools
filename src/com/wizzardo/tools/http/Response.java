package com.wizzardo.tools.http;

import com.wizzardo.tools.io.IOTools;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public class Response {
    private static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat get() {
            SimpleDateFormat format = super.get();
            if (format == null) {           //Sat, 09-Aug-2014 13:12:45 GMT
                format = new SimpleDateFormat("EEE, dd-MMM-yyyy kk:mm:ss z", Locale.US);
                this.set(format);
            }
            return format;
        }
    };

    private HttpURLConnection connection;
    private List<Cookie> cookies;
    private HttpSession session;

    protected Response(HttpURLConnection connection, HttpSession session) {
        this.connection = connection;
        this.session = session;
        if (session != null)
            session.appendCookies(parseCookies());
    }

    public String asString() throws IOException {
        String encoding = connection.getHeaderField("Content-Type");
        if (encoding != null) {
            int i = encoding.indexOf("charset=");
            if (i > 0) {
                encoding = encoding.substring(i + "charset=".length());
            } else {
                encoding = "utf-8";
            }
        } else {
            encoding = "utf-8";
        }
        return asString(encoding);
    }

    public byte[] asBytes() throws IOException {
        return IOTools.bytes(asStream());
    }

    public InputStream asStream() throws IOException {
        InputStream inputStream = connection.getResponseCode() < 400 ? connection.getInputStream() : connection.getErrorStream();
        if ("gzip".equals(connection.getHeaderField("Content-Encoding")))
            inputStream = new GZIPInputStream(inputStream);
        else if ("deflate".equals(connection.getHeaderField("Content-Encoding")))
            inputStream = new DeflaterInputStream(inputStream);
        return inputStream;
    }

    public String asString(String charset) throws IOException {
        byte[] bytes = asBytes();
        return new String(bytes, charset);
    }

    public List<Cookie> getCookies() {
        if (session != null)
            return session.getCookies(connection.getURL());
        else
            return parseCookies();
    }

    private List<Cookie> parseCookies() {
        if (cookies != null)
            return cookies;
        //Set-Cookie: RMID=732423sdfs73242; expires=Fri, 31 Dec 2010 23:59:59 GMT; path=/; domain=.example.net
        List<Cookie> cookies = new ArrayList<Cookie>();

        Map<String, List<String>> headers = connection.getHeaderFields();
        if (headers.containsKey("Set-Cookie"))
            for (String raw : headers.get("Set-Cookie")) {
                String[] data = raw.split("; *");
                String[] kv = data[0].split("=", 2);

                Cookie cookie = new Cookie(kv[0], kv[1]);

                for (int i = 1; i < data.length; i++) {
                    kv = data[i].split("=", 2);
                    if (kv[0].equalsIgnoreCase("expires"))
                        try {
                            cookie.expired = dateFormatThreadLocal.get().parse(kv[1]);
                        } catch (ParseException ignore) {
                        }
                    else if (kv[0].equalsIgnoreCase("path"))
                        cookie.path = kv[1];
                    else if (kv[0].equalsIgnoreCase("domain"))
                        cookie.domain = kv[1];
                }

                if (cookie.path == null)
                    cookie.path = "/";

                if (cookie.domain == null)
                    cookie.domain = connection.getURL().getHost();

                cookies.add(cookie);
            }
        this.cookies = cookies;
        return cookies;
    }

    public String getHeader(String key) {
        return connection.getHeaderField(key);
    }

    public Map<String, List<String>> getHeaders() {
        return connection.getHeaderFields();
    }

    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }
}