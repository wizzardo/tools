package com.wizzardo.tools.aws;

import com.wizzardo.tools.collections.CollectionTools;
import com.wizzardo.tools.collections.flow.Flow;
import com.wizzardo.tools.http.ConnectionMethod;
import com.wizzardo.tools.http.Request;
import com.wizzardo.tools.http.Response;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.misc.SoftThreadLocal;
import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.security.SHA256;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class AwsRequest extends Request {

    protected final static SoftThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new SoftThreadLocal<>(() -> new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US));
    protected final static SoftThreadLocal<SimpleDateFormat> dateFormatShortThreadLocal = new SoftThreadLocal<>(() -> {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        TimeZone tz = TimeZone.getTimeZone("UTC");
        format.setTimeZone(tz);
        return format;
    });
    protected final static SoftThreadLocal<SimpleDateFormat> dateFormatIsoThreadLocal = new SoftThreadLocal<>(() -> {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        TimeZone tz = TimeZone.getTimeZone("UTC");
        format.setTimeZone(tz);
        return format;
    });

    protected String keyId;
    protected String secret;
    protected String host;
    protected String bucket = "";
    protected String region;
    protected String service;
    protected String path;

    public AwsRequest() {
        super(null);
    }

    protected void prepareRequest(ConnectionMethod method) throws IOException {
        url = "https://" + host + bucket + path;

        Date date = new Date();
        String dateShort = dateFormatShortThreadLocal.getValue().format(date);
        String dateIso = dateFormatIsoThreadLocal.getValue().format(date);
        String dateLong = dateFormatThreadLocal.getValue().format(date);

        SHA256 bodySHA = SHA256.create();
        if (data != null)
            bodySHA.update(data.getInputStream());
        String payloadHash = bodySHA.asString();

        this.header("Date", dateLong)
                .header("x-amz-content-sha256", payloadHash)
                .header("x-amz-date", dateIso)
//                .header("x-amz-storage-class", "REDUCED_REDUNDANCY")
        ;

        URL u = Unchecked.call(() -> new URL(url));

        List<Pair<String, String>> headers = Flow.of(
                        Flow.of(this.headers.entrySet()).map(entry -> new Pair<>(entry.getKey().toLowerCase(), entry.getValue())),
                        Flow.of(new Pair<>("host", u.getHost())),
                        Flow.of(() -> data).filter(Objects::nonNull).map(it -> new Pair<>("content-length", String.valueOf(it.length())))
                )
                .toSortedList(Comparator.comparing(o -> o.key))
                .get();

        List<Pair<String, String>> params = Flow.of(this.params.entrySet())
                .map(entry -> new Pair<>(encode(entry.getKey()), encode(CollectionTools.join(entry.getValue(), ","))))
                .toSortedList(Comparator.comparing(o -> o.key))
                .get();

        String canonicalRequest = buildCanonicalRequest(method.name(), u.getPath(), payloadHash, headers, params);
        String requestHash = SHA256.create().update(canonicalRequest).asString();

        byte[] keyBytes = getSignatureKey(secret, dateShort, region, service);
        String data = "AWS4-HMAC-SHA256\n" +
                dateIso + "\n" +
                dateShort + "/" + region + "/" + service + "/aws4_request\n" +
                requestHash;
        String signature = toHexString(hmacSHA256(data, keyBytes), 64);
        String result = "AWS4-HMAC-SHA256 Credential=" + keyId + "/" + dateShort + "/" + region + "/" + service + "/aws4_request," +
                "SignedHeaders=" + Flow.of(headers).map(header -> header.key).join(";").get() + "," +
                "Signature=" + signature;

        header("Authorization", result);
    }

    protected String encode(String value) {
        return Unchecked.call(() -> URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
    }

    protected byte[] hmacSHA256(String data, byte[] key) {
        String algorithm = "HmacSHA256";
        return Unchecked.call(() -> {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        });
    }

    protected String toHexString(byte[] b, int length) {
        String str = new BigInteger(1, b).toString(16);
        while (str.length() < length) {
            str = "0" + str;
        }
        return str;
    }

    protected byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(dateStamp, kSecret);
        byte[] kRegion = hmacSHA256(regionName, kDate);
        byte[] kService = hmacSHA256(serviceName, kRegion);
        byte[] kSigning = hmacSHA256("aws4_request", kService);
        return kSigning;
    }

    protected String buildCanonicalRequest(String method, String path, String payloadHash, List<Pair<String, String>> headers, List<Pair<String, String>> params) {
        StringBuilder sb = new StringBuilder()
                .append(method).append('\n')
                .append(path).append('\n');

        for (int i = 0; i < params.size(); i++) {
            if (i > 0)
                sb.append("&");

            Pair<String, String> param = params.get(i);
            sb.append(param.key).append('=').append(param.value);
        }
        sb.append('\n');

        for (Pair<String, String> header : headers) {
            sb.append(header.key).append(':').append(header.value).append('\n');
        }
        sb.append('\n');

        for (int i = 0; i < headers.size(); i++) {
            if (i > 0)
                sb.append(';');
            sb.append(headers.get(i).key);
        }
        sb.append('\n');
        sb.append(payloadHash);

        return sb.toString();
    }

    public AwsRequest path(String path) {
        if (!path.isEmpty() && !path.startsWith("/"))
            path = "/" + path;

        this.path = path;
        return self();
    }

    public AwsRequest region(String region) {
        this.region = region;
        return self();
    }

    public AwsRequest host(String host) {
        this.host = host;
        return self();
    }


    public AwsRequest service(String service) {
        this.service = service;
        return self();
    }

    public AwsRequest bucket(String bucket) {
        if (bucket != null && !bucket.isEmpty())
            this.bucket = "/" + bucket;
        return self();
    }

    public AwsRequest keyId(String keyId) {
        this.keyId = keyId;
        return self();
    }

    public AwsRequest secret(String secret) {
        this.secret = secret;
        return self();
    }

    @Override
    protected AwsRequest self() {
        return this;
    }

    @Override
    public Response execute() throws IOException {
        prepareRequest(method);
        return super.execute();
    }
}
