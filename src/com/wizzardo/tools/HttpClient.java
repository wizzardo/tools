package com.wizzardo.tools;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.wizzardo.tools.security.Base64;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Moxa
 */
public class HttpClient {

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

    private static String createPostParameters(Map<String, String> params, String urlEncoding) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> iter = params.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            sb.append(entry.getKey()).append("=");
            if (urlEncoding != null) {
                try {
                    sb.append(URLEncoder.encode(entry.getValue(), urlEncoding));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                sb.append(entry.getValue());
            }
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    private static String createURL(String url, Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        Iterator<Map.Entry<String, String>> iter = params.entrySet().iterator();
        if (iter.hasNext()) {
            sb.append("?");
        }
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "utf-8"));
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    public static String getContentAsString(HttpURLConnection conn, String charset) {
        try {
            return getContentAsString(conn.getInputStream(), charset);
        } catch (IOException e) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    /**
     * use UTF-8 as default charset
     */
    public static String getContentAsString(HttpURLConnection conn) {
        return getContentAsString(conn, "utf-8");
    }

    /**
     * use UTF-8 as default charset
     */
    public static String getContentAsString(InputStream in) {
        return getContentAsString(in, "utf-8");
    }

    public static String getContentAsString(InputStream in, String charset) {
        byte[] out = getContent(in);
        if (out != null) {
            try {
                return new String(out, charset);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static byte[] getContent(InputStream in) {
        try {
            int r;
            byte[] b = new byte[10240];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while ((r = in.read(b)) != -1) {
                out.write(b, 0, r);
            }
            return out.toByteArray();
        } catch (IOException e) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public static Request connect(String url) {
        return new Request(url);
    }

    public static enum ConnectionMethod {

        GET("GET"), POST("POST");
        private String method;

        private ConnectionMethod(String method) {
            this.method = method;
        }

        @Override
        public String toString() {
            return method;
        }
    }

    public static class Cookie {
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

    public static class Response {
        private HttpURLConnection connection;

        protected Response(HttpURLConnection connection) {
            this.connection = connection;
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
            InputStream inputStream = connection.getResponseCode() < 400 ? connection.getInputStream() : connection.getErrorStream();
            if ("gzip".equals(connection.getHeaderField("Content-Encoding")))
                inputStream = new GZIPInputStream(inputStream);
            else if ("deflate".equals(connection.getHeaderField("Content-Encoding")))
                inputStream = new DeflaterInputStream(inputStream);
            return getContent(inputStream);
        }

        public String asString(String charset) throws IOException {
            byte[] bytes = asBytes();
            return new String(bytes, charset);
        }

        public List<Cookie> getCookies() throws IOException {
            //Set-Cookie: RMID=732423sdfs73242; expires=Fri, 31 Dec 2010 23:59:59 GMT; path=/; domain=.example.net
            List<Cookie> cookies = new ArrayList<Cookie>();

            for (String raw : connection.getHeaderFields().get("Set-Cookie")) {
                String[] data = raw.split("; ");
                String[] kv = data[0].split("=", 2);

                Cookie cookie = new Cookie(kv[0], kv[1]);

                kv = data[1].split("=", 2);
                try {
                    cookie.expired = dateFormatThreadLocal.get().parse(kv[1]);
                } catch (ParseException ignore) {
                }

                if (data.length > 2) {
                    kv = data[2].split("=", 2);
                    cookie.path = kv[1];
                } else {
                    cookie.path = "/";
                }

                if (data.length > 3) {
                    kv = data[3].split("=", 2);
                    cookie.domain = kv[1];
                } else {
                    cookie.domain = connection.getURL().getHost();
                }
                cookies.add(cookie);
            }

            return cookies;
        }

        public String getHeader(String key) {
            return connection.getHeaderField(key);
        }

        public int getResponseCode() throws IOException {
            return connection.getResponseCode();
        }
    }

    public static enum ContentType {
        BINARY("application/octet-stream");

        public final String text;

        ContentType(String text) {
            this.text = text;
        }
    }

    public static class Request {

        private int maxRetryCount = 0;
        private long pauseBetweenRetries = 0;
        private ConnectionMethod method = ConnectionMethod.GET;
        private LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        private HashMap<String, String> headers = new HashMap<String, String>();
        private HashMap<String, byte[]> dataArrays = new HashMap<String, byte[]>();
        private HashMap<String, String> dataTypes = new HashMap<String, String>();
        private String url;
        private boolean multipart = false;
        private String charsetForEncoding;
        private Proxy proxy;
        private boolean redirects = true;
        private byte[] data;

        public Request(String url) {
            this.url = url;
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.187 Safari/535.1");
            headers.put("Accept-Encoding", "gzip, deflate");
//            headers.put("Accept-Charset", "windows-1251,utf-8;q=0.7,*;q=0.3");
        }

        public Request setMaxRetryCount(int n) {
            maxRetryCount = n;
            return this;
        }

        public Request setBasicAuthentication(String user, String password) {
            header("Authorization", "Basic " + Base64.encodeToString((user + ":" + password).getBytes()));
            return this;
        }

        public Request setProxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Request maxRetryCount(int n) {
            maxRetryCount = n;
            return this;
        }

        public Request setPauseBetweenRetries(long pause) {
            pauseBetweenRetries = pause;
            return this;
        }

        public Request pauseBetweenRetries(long pause) {
            pauseBetweenRetries = pause;
            return this;
        }

        public Request setMethod(ConnectionMethod method) {
            this.method = method;
            return this;
        }

        public Request method(ConnectionMethod method) {
            this.method = method;
            return this;
        }

        public Request setCookies(String cookie) {
            headers.put("Cookie", cookie);
            return this;
        }

        public Request cookies(String cookie) {
            headers.put("Cookie", cookie);
            return this;
        }

        public Request cookies(List<Cookie> cookies) {
            StringBuilder sb = new StringBuilder();
            for (Cookie c : cookies) {
                if (sb.length() > 0)
                    sb.append("; ");
                sb.append(c.key).append("=").append(c.value);
            }
            headers.put("Cookie", sb.toString());
            return this;
        }

        public Request setCookies(List<Cookie> cookies) {
            return cookies(cookies);
        }

        public Request setReferer(String referer) {
            headers.put("Referer", referer);
            return this;
        }

        public Request referer(String referer) {
            headers.put("Referer", referer);
            return this;
        }

        public Request setJson(String json) {
            return json(json);
        }

        public Request json(String json) {
            try {
                data = json.getBytes("utf-8");
            } catch (UnsupportedEncodingException ignored) {
            }
            setContentType("application/json; charset=utf-8");
            return this;
        }

        public Request setXml(String xml) {
            return xml(xml);
        }

        public Request xml(String xml) {
            try {
                data = xml.getBytes("utf-8");
            } catch (UnsupportedEncodingException ignored) {
            }
            setContentType("text/xml; charset=utf-8");
            return this;
        }

        public Request setData(byte[] data, String contentType) {
            return data(data, contentType);
        }

        public Request data(byte[] data, String contentType) {
            this.data = data;
            setContentType(contentType);
            return this;
        }

        public Request addParameter(String key, String value) {
            params.put(key, value);
            return this;
        }

        public Request setUrlEncoding(String charset) {
            charsetForEncoding = charset;
            return this;
        }

        public Request disableRedirects() {
            redirects = false;
            return this;
        }

        public Request addFile(String key, File value) {
            return addFile(key, value.getAbsolutePath());
        }

        public Request addFile(String key, String path) {
            multipart = true;
            method = ConnectionMethod.POST;
            params.put(key, "file://" + path);
            return this;
        }

        public Request addByteArray(String key, byte[] array, String name) {
            return addByteArray(key, array, name, null);
        }

        public Request addByteArray(String key, byte[] array, String name, String type) {
            multipart = true;
            method = ConnectionMethod.POST;
            params.put(key, "array://" + name);
            dataArrays.put(key, array);
            if (type != null) {
                dataTypes.put(key, type);
            }
            return this;
        }

        public Request data(String key, String value) {
            params.put(key, value);
            return this;
        }

        public Request setHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Request header(String key, String value) {
            headers.put(key, value);
            return this;
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

        private Response connect(int retryNumber) throws IOException {
            try {
                if (method == ConnectionMethod.GET || (method == ConnectionMethod.POST && data != null)) {
                    url = createURL(url, params);
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
                        for (Map.Entry<String, String> param : params.entrySet()) {
                            out.write("------WebKitFormBoundaryZzaC4MkAfrAMfJCJ\r\n".getBytes());
                            String type = dataTypes.get(param.getKey()) != null ? dataTypes.get(param.getKey()) : "application/octet-stream";
                            if (param.getValue().startsWith("file://")) {
                                File f = new File(param.getValue().substring(7));
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
                            } else if (param.getValue().startsWith("array://")) {
                                out.write(("Content-Disposition: form-data; name=\"" + param.getKey() + "\"; filename=\"" + param.getValue().substring(8) + "\"\r\n").getBytes());
                                out.write(("Content-Type: " + type + "\r\n").getBytes());
                                out.write("\r\n".getBytes());
                                out.write(dataArrays.get(param.getKey()));
                            } else {
                                out.write(("Content-Disposition: form-data; name=\"" + param.getKey() + "\"" + "\r\n\r\n").getBytes());
                                out.write((param.getValue()).getBytes());
                            }
                            out.write("\r\n".getBytes());
                        }
                        out.write("------WebKitFormBoundaryZzaC4MkAfrAMfJCJ--".getBytes());
                        out.flush();
                        out.close();
                    }
                }
                return new Response(c);
            } catch (SocketTimeoutException e) {
                Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, e);
                if (retryNumber < maxRetryCount) {
                    try {
                        Thread.sleep(pauseBetweenRetries);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    return connect(++retryNumber);
                }
            }
            throw new SocketTimeoutException();
        }

        private int getLength() {
            int l = 0;
            l += "------WebKitFormBoundaryZzaC4MkAfrAMfJCJ\r\n".length() * (params.size() + 1) + 2;
            for (Entry<String, String> en : params.entrySet()) {
                if (en.getValue().startsWith("file://") || en.getValue().startsWith("array://")) {
                    String type = dataTypes.get(en.getKey()) != null ? dataTypes.get(en.getKey()) : "application/octet-stream";
                    l += ("Content-Type: " + type + "\r\n").length();
                    if (en.getValue().startsWith("file://")) {
                        l += "Content-Disposition: form-data; name=\"\"; filename=\"\"\r\n\r\n\r\n".length() + en.getKey().getBytes().length + new File(en.getValue().substring(7)).getName().getBytes().length + en.getValue().length();
                    } else {
                        l += "Content-Disposition: form-data; name=\"\"; filename=\"\"\r\n\r\n\r\n".length() + en.getKey().getBytes().length + dataArrays.get(en.getKey()).length + en.getValue().length();
                    }
                } else {
                    l += "Content-Disposition: form-data; name=\"\"\r\n\r\n\r\n".length() + en.getKey().getBytes().length + en.getValue().getBytes().length;
                }
            }
//            System.out.println(l);
            return l;
        }

        public String getUrl() {
            return url;
        }

        public String url() {
            return url;
        }

        public Request setContentType(String contentType) {
            setHeader("Content-Type", contentType);
            return this;
        }
    }

}
