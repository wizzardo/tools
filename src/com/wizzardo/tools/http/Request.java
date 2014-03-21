package com.wizzardo.tools.http;

import com.wizzardo.tools.misc.WrappedException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
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
        return url;
    }

    public Response connect() throws IOException {
        return connect(0);
    }

    public Response get() throws IOException {
        setMethod(ConnectionMethod.GET);
        return connect(0);
    }

    public Response post() throws IOException {
        setMethod(ConnectionMethod.POST);
        return connect(0);
    }

    protected Request setSession(HttpSession session) {
        this.session = session;
        return this;
    }

    @Override
    protected Request self() {
        return this;
    }

    private Response connect(int retryNumber) throws IOException {
        try {
            if (method == ConnectionMethod.GET || (method == ConnectionMethod.POST && data != null)) {
                url = createURL(url.toString(), params);
            }
            URL u = new URL(url);
            HttpURLConnection c;
            if (proxy != null) {
                c = (HttpURLConnection) u.openConnection(proxy);
            } else {
                c = (HttpURLConnection) u.openConnection();
            }
            c.setInstanceFollowRedirects(redirects);
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
            if (method.equals(ConnectionMethod.POST)) {
                c.setDoOutput(true);
                if (!multipart) {
                    if (data == null) {
                        c.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        data = createPostParameters(params, charsetForEncoding).getBytes(charsetForEncoding);
                    }

                    c.addRequestProperty("Content-Length", String.valueOf(data.length));
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
                            String type = dataTypes.get(param.getKey()) != null ? dataTypes.get(param.getKey()) : ContentType.BINARY.text;
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
            return new Response(c, session);
        } catch (SocketTimeoutException e) {
            if (retryNumber < maxRetryCount) {
                try {
                    Thread.sleep(pauseBetweenRetries);
                } catch (InterruptedException ex1) {
                    throw new WrappedException(ex1);
                }
                return connect(++retryNumber);
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
                    String type = dataTypes.get(en.getKey()) != null ? dataTypes.get(en.getKey()) : ContentType.BINARY.text;
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

                sb.append(entry.getKey()).append("=");
                if (urlEncoding != null) {
                    sb.append(URLEncoder.encode(value, urlEncoding));
                } else {
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
            sb.append("?").append(createPostParameters(params, null));

        return sb.toString();
    }

}