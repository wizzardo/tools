package org.bordl.utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.bordl.utils.security.Base64;
import org.bordl.utils.security.MD5;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Moxa
 */
public class HttpClient {

    private static String createPostParameters(Map<String, String> params) {
        return createPostParameters(params, null);
    }

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
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public static Connection connect(String url) {
        return new Connection(url);
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

    public static class Connection {

        private int maxRetryCount = 0;
        private long pauseBetweenRetries = 0;
        private ConnectionMethod method = ConnectionMethod.GET;
        private LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        private HashMap<String, String> headers = new HashMap<String, String>();
        private HashMap<String, byte[]> dataArrays = new HashMap<String, byte[]>();
        private HashMap<String, String> dataTypes = new HashMap<String, String>();
        private String url;
        private String json;
        private boolean multipart = false;
        private String charsetForEncoding;
        private Proxy proxy;
        private boolean redirects = true;

        public Connection(String url) {
            this.url = url;
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.187 Safari/535.1");
//            headers.put("Accept-Charset", "windows-1251,utf-8;q=0.7,*;q=0.3");
        }

        public Connection setMaxRetryCount(int n) {
            maxRetryCount = n;
            return this;
        }

        public Connection setBasicAuthentication(String user, String password) {
            header("Authorization", "Basic " + Base64.encodeToString((user + ":" + password).getBytes()));
            return this;
        }

        public Connection setProxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Connection maxRetryCount(int n) {
            maxRetryCount = n;
            return this;
        }

        public Connection setPauseBetweenRetries(long pause) {
            pauseBetweenRetries = pause;
            return this;
        }

        public Connection pauseBetweenRetries(long pause) {
            pauseBetweenRetries = pause;
            return this;
        }

        public Connection setMethod(ConnectionMethod method) {
            this.method = method;
            return this;
        }

        public Connection method(ConnectionMethod method) {
            this.method = method;
            return this;
        }

        public Connection setCookie(String cookie) {
            headers.put("Cookie", cookie);
            return this;
        }

        public Connection cookie(String cookie) {
            headers.put("Cookie", cookie);
            return this;
        }

        public Connection setReferer(String referer) {
            headers.put("Referer", referer);
            return this;
        }

        public Connection referer(String referer) {
            headers.put("Referer", referer);
            return this;
        }

        public Connection setJson(String json) {
            this.json = json;
            return this;
        }

        public Connection json(String json) {
            this.json = json;
            return this;
        }

        public Connection addParameter(String key, String value) {
            params.put(key, value);
            return this;
        }

        public Connection setUrlEncoding(String charset) {
            charsetForEncoding = charset;
            return this;
        }

        public Connection disableRedirects() {
            redirects=false;
            return this;
        }

        public Connection addFile(String key, File value) {
            return addFile(key, value.getAbsolutePath());
        }

        public Connection addFile(String key, String path) {
            multipart = true;
            method = ConnectionMethod.POST;
            params.put(key, "file://" + path);
            return this;
        }

        public Connection addByteArray(String key, byte[] array, String name) {
            return addByteArray(key, array, name, null);
        }

        public Connection addByteArray(String key, byte[] array, String name, String type) {
            multipart = true;
            method = ConnectionMethod.POST;
            params.put(key, "array://" + name);
            dataArrays.put(key, array);
            if (type != null) {
                dataTypes.put(key, type);
            }
            return this;
        }

        public Connection data(String key, String value) {
            params.put(key, value);
            return this;
        }

        public Connection setHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Connection header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public HttpURLConnection connect() throws IOException {
            return connect(0);
        }

        public String getAsString() throws IOException {
            return getContentAsString(connect());
        }

        public byte[] getAsBytes() throws IOException {
            return getContent(connect().getInputStream());
        }

        public String getAsString(String charset) throws IOException {
            return getContentAsString(connect(), charset);
        }

        public HttpURLConnection get() throws IOException {
            setMethod(ConnectionMethod.GET);
            return connect(0);
        }

        public HttpURLConnection post() throws IOException {
            setMethod(ConnectionMethod.POST);
            return connect(0);
        }

        private HttpURLConnection connect(int retryNumber) throws IOException {
            try {
                if (method.equals(ConnectionMethod.GET)) {
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
                        String data;
                        if (json != null) {
                            c.addRequestProperty("Content-Type", "application/json; charset=utf-8");
                            data = json;
                        } else {
                            c.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            data = createPostParameters(params, charsetForEncoding);
                        }
                        c.addRequestProperty("Content-Length", String.valueOf(data.length()));
                        OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream());
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
                return c;
            } catch (SocketTimeoutException e) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, e);
                if (retryNumber < maxRetryCount) {
                    try {
                        Thread.sleep(pauseBetweenRetries);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex1);
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
    }

    public static void main(String[] args) throws IOException {

        byte[] bytes = new byte[1024 * 100];
        Random r = new Random();
        r.nextBytes(bytes);

        String md5 = MD5.getMD5AsString(bytes);

        HttpClient.connect("http://localhost:8080/upload")
                .method(HttpClient.ConnectionMethod.POST)
                .addParameter("textField", "ololo")
                .addByteArray("file", bytes, "test_data.bin")
                .getAsString();
    }
}
