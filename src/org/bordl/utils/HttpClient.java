package org.bordl.utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.bordl.utils.security.MD5;

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

    public static InputStream doPostMultiPartRequest(String url, HashMap<String, String> params, HashMap<String, File> files) {

        return null;
    }

    private static int getLength(HashMap<String, String> params, HashMap<String, File> files) {
        int l = 0;
        l += "------WebKitFormBoundaryZzaC4MkAfrAMfJCJ\r\n".length() * (params.size() + files.size() + 1) + 2;
        for (Entry<String, String> en : params.entrySet()) {
            l += "Content-Disposition: form-data; name=\"\"\r\n\r\n\r\n".length() + en.getKey().getBytes().length + en.getValue().getBytes().length;
        }
        for (Entry<String, File> en : files.entrySet()) {
            l += "Content-Type: image/png\r\n".length();
            l += "Content-Disposition: form-data; name=\"\"; filename=\"\"\r\n\r\n\r\n".length() + en.getKey().getBytes().length + en.getValue().getName().getBytes().length + en.getValue().length();
        }
        return l;
    }

    public static void main(String[] args) throws Exception {
        URL u = new URL("http://moxa.no-ip.biz/post/");
        System.out.println("length: " + getLength(new HashMap<String, String>() {

            {
                put("textfield", "dsfgsdf");
            }
        }, new HashMap<String, File>() {

            {
                put("filefield", new File("C:\\favicon.png"));
            }
        }));
        HttpURLConnection hc = (HttpURLConnection) u.openConnection();
//        HttpURLConnection hc = (HttpURLConnection) u.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost",8080)));
        File f = new File("C:\\favicon.png");
        System.out.println(MD5.getMD5AsString(new FileInputStream(f)));
        hc.setDoOutput(true);
        hc.setRequestMethod("POST");
        hc.setRequestProperty("Connection", "Keep-Alive");
        hc.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryZzaC4MkAfrAMfJCJ");
        hc.setRequestProperty("Content-Length", f.length() * 2 + "");
        hc.setChunkedStreamingMode(10240);
        hc.connect();
        OutputStream out = hc.getOutputStream();
        out.write("------WebKitFormBoundaryZzaC4MkAfrAMfJCJ\r\n".getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + "textfield" + "\"" + "\r\n\r\n").getBytes());
        out.write(("testtestetst" + "\r\n").getBytes());

        out.write("------WebKitFormBoundaryZzaC4MkAfrAMfJCJ\r\n".getBytes());
        String fieldName = "file";
        out.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + f.getName() + "\"\r\n").getBytes());
        out.write("Content-Type: application/octet-stream\r\n".getBytes());
        out.write("\r\n".getBytes());
        FileInputStream in = new FileInputStream(f);
        int r = 0;
        byte[] b = new byte[10240];

        while ((r = in.read(b)) != -1) {
            out.write(b, 0, r);
            out.flush();
        }
        out.write("\r\n".getBytes());
        out.write("------WebKitFormBoundaryZzaC4MkAfrAMfJCJ--".getBytes());
        out.flush();
        out.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(hc.getInputStream()));
        while (br.ready()) {
            System.out.println(br.readLine());
        }
    }
}
