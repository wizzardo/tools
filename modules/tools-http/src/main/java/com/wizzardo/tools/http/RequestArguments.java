package com.wizzardo.tools.http;

import com.wizzardo.tools.security.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.util.*;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public class RequestArguments<T extends RequestArguments> {

    protected int maxRetryCount = 0;
    protected long pauseBetweenRetries = 0;
    protected ConnectionMethod method = ConnectionMethod.GET;
    protected Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
    protected Map<String, String> headers = new HashMap<String, String>();
    protected Map<String, byte[]> dataArrays = new HashMap<String, byte[]>();
    protected Map<String, String> dataTypes = new HashMap<String, String>();
    protected boolean multipart = false;
    protected String charsetForEncoding = "utf-8";
    protected Proxy proxy;
    protected boolean redirects = true;
    protected Body data;
    protected HostnameVerifier hostnameVerifier;
    protected SSLSocketFactory sslFactory;
    protected int connectTimeout;
    protected int readTimeout;

    public Request createRequest(String url) {
        Request request = new Request(url)
                .addHeaders(headers)
                .addParameterLists(params)
                .setPauseBetweenRetries(pauseBetweenRetries)
                .setMaxRetryCount(maxRetryCount)
                .setProxy(proxy)
                .setSSLSocketFactory(sslFactory)
                .setHostnameVerifier(hostnameVerifier)
                .method(method)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .setUrlEncoding(charsetForEncoding);

        request.data = data;
        request.dataTypes = dataTypes;
        request.dataArrays = dataArrays;
        request.redirects = redirects;
        request.multipart = multipart;

        return request;
    }

    protected T self() {
        return (T) this;
    }

    public ConnectionMethod method() {
        return method;
    }

    public T setMaxRetryCount(int n) {
        maxRetryCount = n;
        return self();
    }

    public T setBasicAuthentication(String user, String password) {
        header("Authorization", "Basic " + Base64.encodeToString((user + ":" + password).getBytes()));
        return self();
    }

    public T setProxy(Proxy proxy) {
        this.proxy = proxy;
        return self();
    }

    public Proxy getProxy() {
        return proxy;
    }

    public T maxRetryCount(int n) {
        maxRetryCount = n;
        return self();
    }

    public T setPauseBetweenRetries(long pause) {
        pauseBetweenRetries = pause;
        return self();
    }

    public T pauseBetweenRetries(long pause) {
        pauseBetweenRetries = pause;
        return self();
    }

    public T setMethod(ConnectionMethod method) {
        this.method = method;
        return self();
    }

    public T method(ConnectionMethod method) {
        this.method = method;
        return self();
    }

    public T setCookies(String cookie) {
        headers.put("Cookie", cookie);
        return self();
    }

    public T cookies(String cookie) {
        headers.put("Cookie", cookie);
        return self();
    }

    public T cookies(List<Cookie> cookies) {
        StringBuilder sb = new StringBuilder();
        for (Cookie c : cookies) {
            if (sb.length() > 0)
                sb.append("; ");
            sb.append(c.key).append("=").append(c.value);
        }
        if (sb.length() > 0)
            headers.put("Cookie", sb.toString());
        return self();
    }

    public T setCookies(List<Cookie> cookies) {
        return cookies(cookies);
    }

    public T setReferer(String referer) {
        headers.put("Referer", referer);
        return self();
    }

    public T referer(String referer) {
        headers.put("Referer", referer);
        return self();
    }

    public T setJson(String json) {
        return json(json);
    }

    public T json(String json) {
        try {
            return data(json.getBytes("utf-8"), ContentType.JSON);
        } catch (UnsupportedEncodingException ignored) {
        }
        return self();
    }

    public T setXml(String xml) {
        return xml(xml);
    }

    public T xml(String xml) {
        try {
            return data(xml.getBytes("utf-8"), ContentType.XML);
        } catch (UnsupportedEncodingException ignored) {
        }
        return self();
    }

    public T setData(byte[] data, String contentType) {
        return data(data, contentType);
    }

    public Body getData() {
        return data;
    }

    public Body data() {
        return data;
    }

    public T data(byte[] data, String contentType) {
        this.data = new Body.ByteArrayBody(data);
        method = ConnectionMethod.POST;
        setContentType(contentType);
        return self();
    }

    public T data(byte[] data, ContentType contentType) {
        return data(data, contentType.value);
    }

    public T data(File data, String contentType) {
        this.data = new Body.FileBody(data);
        method = ConnectionMethod.POST;
        setContentType(contentType);
        return self();
    }

    public T data(File data, ContentType contentType) {
        return data(data, contentType.value);
    }

    public T removeParameter(String key) {
        params.remove(key);
        dataArrays.remove(key);
        dataTypes.remove(key);
        return self();
    }

    public T addParameter(String key, Object value) {
        return addParameter(key, String.valueOf(value));
    }

    public T addParameter(String key, String value) {
        List<String> l = params.get(key);
        if (l == null) {
            l = new ArrayList<String>();
            params.put(key, l);
        }
        l.add(value);
        return self();
    }

    public T addParameters(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            addParameter(entry.getKey(), entry.getValue());
        }
        return self();
    }

    public String param(String key) {
        List<String> strings = params(key);
        return strings == null || strings.isEmpty() ? null : strings.get(0);
    }

    public List<String> params(String key) {
        return params.get(key);
    }

    public T param(String key, String value) {
        return addParameter(key, value);
    }

    public T param(String key, Object value) {
        return addParameter(key, value);
    }

    public T params(Map<String, String> params) {
        return addParameters(params);
    }

    public T addParameterList(String key, List<String> values) {
        List<String> l = params.get(key);
        if (l == null) {
            l = new ArrayList<String>();
            params.put(key, l);
        }
        l.addAll(values);
        return self();
    }

    public T addParameterLists(Map<String, List<String>> params) {
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            addParameterList(entry.getKey(), entry.getValue());
        }
        return self();
    }

    public Map<String, List<String>> params() {
        return params;
    }

    public T setUrlEncoding(String charset) {
        charsetForEncoding = charset;
        return self();
    }

    public T disableRedirects() {
        redirects = false;
        return self();
    }

    public T addFile(String key, File value) {
        return addFile(key, value.getAbsolutePath());
    }

    public T addFile(String key, String path) {
        return addFile(key, path, null);
    }

    public T addFile(String key, String path, String type) {
        multipart = true;
        method = ConnectionMethod.POST;
        addParameter(key, "file://" + path);
        if (type != null)
            dataTypes.put(key, type);

        return self();
    }

    public T addByteArray(String key, byte[] array, String name) {
        return addByteArray(key, array, name, null);
    }

    public T addByteArray(String key, byte[] array, String name, String type) {
        multipart = true;
        method = ConnectionMethod.POST;
        addParameter(key, "array://" + name);
        dataArrays.put(key, array);
        if (type != null) {
            dataTypes.put(key, type);
        }
        return self();
    }

    public Map<String, String> headers() {
        return headers;
    }

    public T addHeader(String key, String value) {
        headers.put(key, value);
        return self();
    }

    public T removeHeader(String key) {
        headers.remove(key);
        return self();
    }

    public T addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return self();
    }

    public T header(String key, String value) {
        headers.put(key, value);
        return self();
    }

    public String header(String key) {
        return headers.get(key);
    }

    public T setHostnameVerifier(HostnameVerifier hv) {
        this.hostnameVerifier = hv;
        return self();
    }

    public T setSSLSocketFactory(SSLSocketFactory sslFactory) {
        this.sslFactory = sslFactory;
        return self();
    }

    public T timeout(int ms) {
        this.connectTimeout = ms;
        this.readTimeout = ms;
        return self();
    }

    public T readTimeout(int ms) {
        this.readTimeout = ms;
        return self();
    }

    public T connectTimeout(int ms) {
        this.connectTimeout = ms;
        return self();
    }

    public T setContentType(String contentType) {
        return addHeader("Content-Type", contentType);
    }

    public T setContentType(ContentType contentType) {
        return addHeader("Content-Type", contentType.value);
    }
}