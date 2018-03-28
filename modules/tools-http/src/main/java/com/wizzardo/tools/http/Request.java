package com.wizzardo.tools.http;

import com.wizzardo.tools.misc.Unchecked;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public class Request extends RequestArguments<Request> {

    protected String url;
    protected HttpSession session;

    public Request(String url) {
        this.url = url;
    }

    @Override
    public Request createRequest(String url) {
        return super.createRequest(url).setSession(session);
    }

    public String getUrl() {
        try {
            return createURL(url, params);
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    public Response execute() throws IOException {
        return execute(0);
    }

    public Response get() throws IOException {
        setMethod(ConnectionMethod.GET);
        return execute();
    }

    public Response post() throws IOException {
        setMethod(ConnectionMethod.POST);
        return execute();
    }

    public Response put() throws IOException {
        setMethod(ConnectionMethod.PUT);
        return execute();
    }

    public Response head() throws IOException {
        setMethod(ConnectionMethod.HEAD);
        return execute();
    }

    public Response delete() throws IOException {
        setMethod(ConnectionMethod.DELETE);
        return execute();
    }

    public Response options() throws IOException {
        setMethod(ConnectionMethod.OPTIONS);
        return execute();
    }

    public Response trace() throws IOException {
        setMethod(ConnectionMethod.TRACE);
        return execute();
    }

    protected Request setSession(HttpSession session) {
        this.session = session;
        return this;
    }

    @Override
    protected Request self() {
        return this;
    }

    protected Response execute(int retryNumber) throws IOException {
        try {
            String url = this.url;
            if (!url.toLowerCase().startsWith("http"))
                url = "http://" + url;

            if (data != null || (method != ConnectionMethod.PUT && method != ConnectionMethod.POST)) {
                url = createURL(url, params);
            }
            URL u = new URL(url);
            HttpURLConnection c;
            if (proxy != null) {
                c = (HttpURLConnection) u.openConnection(proxy);
            } else {
                c = (HttpURLConnection) u.openConnection();
            }
            c.setConnectTimeout(connectTimeout);
            c.setReadTimeout(readTimeout);
            c.setInstanceFollowRedirects(false);
            c.setRequestMethod(method.toString());
            for (Map.Entry<String, String> header : headers.entrySet()) {
                c.setRequestProperty(header.getKey(), header.getValue());
            }
            if (hostnameVerifier != null && url.startsWith("https")) {
                HttpsURLConnection https = (HttpsURLConnection) c;
                https.setHostnameVerifier(hostnameVerifier);
            }
            if (sslFactory != null && url.startsWith("https")) {
                HttpsURLConnection https = (HttpsURLConnection) c;
                https.setSSLSocketFactory(sslFactory);
            }
            if (method == ConnectionMethod.POST || method == ConnectionMethod.PUT) {
                c.setDoOutput(true);
                if (!multipart) {
                    if (data == null) {
                        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        data = createPostParameters(params, charsetForEncoding).getBytes(charsetForEncoding);
                    }

                    c.setRequestProperty("Content-Length", String.valueOf(data.length));
                    OutputStream out = c.getOutputStream();
                    out.write(data);
                    out.flush();
                    out.close();
                } else {
                    c.setRequestProperty("Connection", "Keep-Alive");
                    c.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryZzaC4MkAfrAMfJCJ");
                    c.setRequestProperty("Content-Length", String.valueOf(getLength()));
//                        c.setChunkedStreamingMode(10240);
                    OutputStream out = c.getOutputStream();
                    for (Map.Entry<String, List<String>> param : params.entrySet()) {
                        for (String value : param.getValue()) {
                            out.write("------WebKitFormBoundaryZzaC4MkAfrAMfJCJ\r\n".getBytes());
                            String type = dataTypes.get(param.getKey()) != null ? dataTypes.get(param.getKey()) : ContentType.BINARY.value;
                            if (value.startsWith("file://")) {
                                File f = new File(value.substring(7));
                                out.write(("Content-Disposition: form-data; name=\"" + param.getKey() + "\"; filename=\"" + f.getName() + "\"\r\n").getBytes());
                                out.write(("Content-Type: " + type + "\r\n").getBytes());
                                out.write("\r\n".getBytes());
                                FileInputStream in = new FileInputStream(f);
                                int r = 0;
                                byte[] b = new byte[10240];
//                                long rr = 0;
                                while ((r = in.read(b)) != -1) {
                                    out.write(b, 0, r);
                                    out.flush();
//                                    rr += r;
//                                    System.out.println(100f * rr / f.length());
                                }
                                in.close();
                            } else if (value.startsWith("array://")) {
                                out.write(("Content-Disposition: form-data; name=\"" + param.getKey() + "\"; filename=\"" + value.substring(8) + "\"\r\n").getBytes());
                                out.write(("Content-Type: " + type + "\r\n").getBytes());
                                out.write("\r\n".getBytes());
                                out.write(dataArrays.get(param.getKey()));
                            } else {
                                out.write(("Content-Disposition: form-data; name=\"" + param.getKey() + "\"" + "\r\n\r\n").getBytes());
                                out.write(value.getBytes());
                            }
                            out.write("\r\n".getBytes());
                        }
                    }
                    out.write("------WebKitFormBoundaryZzaC4MkAfrAMfJCJ--".getBytes());
                    out.flush();
                    out.close();
                }
            }

            int responseCode = c.getResponseCode();
            if (redirects && responseCode >= 300 && responseCode < 400) {
                if (session == null)
                    session = new HttpSession();

                Response r = new Response(this, c);
                String path = r.getHeader("Location");
                if (!path.startsWith("http://") && !path.startsWith("https://"))
                    path = u.getProtocol() + "://" + u.getHost() + path;

                return session.createRequest(path).get();
            }
            return new Response(this, c);
        } catch (SocketTimeoutException e) {
            if (retryNumber < maxRetryCount) {
                try {
                    Thread.sleep(pauseBetweenRetries);
                } catch (InterruptedException ex1) {
                    throw Unchecked.rethrow(ex1);
                }
                return execute(++retryNumber);
            }
        }
        throw new SocketTimeoutException();
    }

    private int getLength() {
        int l = 0;
        l += "------WebKitFormBoundaryZzaC4MkAfrAMfJCJ\r\n".length() * (params.size() + 1) + 2;
        for (Map.Entry<String, List<String>> en : params.entrySet()) {
            for (String value : en.getValue()) {
                if (value.startsWith("file://") || value.startsWith("array://")) {
                    String type = dataTypes.get(en.getKey()) != null ? dataTypes.get(en.getKey()) : ContentType.BINARY.value;
                    l += ("Content-Type: " + type + "\r\n").length();
                    if (value.startsWith("file://")) {
                        l += "Content-Disposition: form-data; name=\"\"; filename=\"\"\r\n\r\n\r\n".length() + en.getKey().getBytes().length + new File(value.substring(7)).getName().getBytes().length + value.length();
                    } else {
                        l += "Content-Disposition: form-data; name=\"\"; filename=\"\"\r\n\r\n\r\n".length() + en.getKey().getBytes().length + dataArrays.get(en.getKey()).length + value.length();
                    }
                } else {
                    l += "Content-Disposition: form-data; name=\"\"\r\n\r\n\r\n".length() + en.getKey().getBytes().length + value.getBytes().length;
                }
            }
        }
//            System.out.println(l);
        return l;
    }

    private String createPostParameters(Map<String, List<String>> params, String urlEncoding) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, List<String>>> iter = params.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<String>> entry = iter.next();

            boolean and = false;
            for (String value : entry.getValue()) {
                if (and)
                    sb.append('&');
                else
                    and = true;

                if (urlEncoding != null) {
                    sb.append(URLEncoder.encode(entry.getKey(), urlEncoding)).append("=");
                    sb.append(URLEncoder.encode(value, urlEncoding));
                } else {
                    sb.append(URLEncoder.encode(entry.getKey(), "utf-8")).append("=");
                    sb.append(URLEncoder.encode(value, "utf-8"));
                }
            }
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    private String createURL(String url, Map<String, List<String>> params) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        if (!params.isEmpty())
            sb.append("?").append(createPostParameters(params, null).replace("+", "%20"));

        return sb.toString();
    }

}