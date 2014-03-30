package com.wizzardo.tools.http;

import com.wizzardo.tools.misc.WrappedException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: wizzardo
 * Date: 3/5/14
 */
public class HttpSession extends RequestArguments<HttpSession> {

    private Map<String, List<Cookie>> cookies = new ConcurrentHashMap<String, List<Cookie>>();

    public HttpSession() {
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.187 Safari/535.1");
        headers.put("Accept-Encoding", "gzip, deflate");
//            headers.put("Accept-Charset", "windows-1251,utf-8;q=0.7,*;q=0.3");
    }

    public Request createRequest(String url) {
        return super.createRequest(url)
                .setSession(this)
                .setCookies(getCookies(url));
    }

    @Override
    protected HttpSession self() {
        return this;
    }

    public List<Cookie> getCookies(String url) {
        try {
            return getCookies(new URL(url));
        } catch (MalformedURLException e) {
            throw new WrappedException(e);
        }
    }

    public Map<String, List<Cookie>> getCookies() {
        return cookies;
    }

    public List<Cookie> getCookies(URL url) {
        List<Cookie> cookies = new ArrayList<Cookie>();
        String domain = url.getHost();
        while (domain.length() > 0) {
//            System.out.println(domain);
            List<Cookie> l = this.cookies.get(domain);
            if (l != null)
                for (Cookie cookie : l)
                    if ((url.getPath().isEmpty() ? "/" : url.getPath()).startsWith(cookie.path))
                        cookies.add(cookie);

            int index = domain.indexOf('.');
            if (index >= 0)
                domain = domain.substring(index + 1);
            else
                break;
        }

        return cookies;
    }

    public synchronized void appendCookies(List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            String domain = cookie.domain.startsWith(".") ? cookie.domain.substring(1) : cookie.domain;
            List<Cookie> l = this.cookies.get(domain);
            if (cookie.value.equalsIgnoreCase("deleted") && l != null) {
                Iterator<Cookie> i = l.iterator();
                while (i.hasNext()) {
                    Cookie it = i.next();
                    if (it.key.equals(cookie.key) && it.domain.equals(cookie.domain))
                        i.remove();
                }
            } else {
                if (l == null) {
                    l = new ArrayList<Cookie>();
                    this.cookies.put(domain, l);
                }
                l.add(cookie);
            }
        }
    }
}
