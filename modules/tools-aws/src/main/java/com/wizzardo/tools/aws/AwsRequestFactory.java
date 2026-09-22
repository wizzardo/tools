package com.wizzardo.tools.aws;

import com.wizzardo.tools.http.ConnectionMethod;
import com.wizzardo.tools.http.Request;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AwsRequestFactory {

    protected CredentialsProvider credentialsProvider;
    protected String host;
    protected String bucket;
    protected String region;
    protected String service;

    public AwsRequestFactory(CredentialsProvider credentialsProvider,
                             String host,
                             String bucket,
                             String region,
                             String service
    ) {
        this.credentialsProvider = credentialsProvider;
        this.host = host;
        this.bucket = bucket;
        this.region = region;
        this.service = service;
    }

    public AwsRequestFactory(CredentialsProvider credentialsProvider,
                             String host,
                             String region,
                             String service
    ) {
        this.credentialsProvider = credentialsProvider;
        this.host = host;
        this.region = region;
        this.service = service;
    }

    public Request createRequest(String path) {
        CredentialsProvider.AwsCredentials credentials = credentialsProvider.get();
        AwsRequest request = new AwsRequest()
                .host(host)
                .bucket(bucket)
                .region(region)
                .service(service)
                .keyId(credentials.AccessKeyId)
                .secret(credentials.SecretAccessKey)
                .path(path);

        if (credentials.Token != null)
            request.header("x-amz-security-token", credentials.Token);

        return request;
    }

    /**
     * Generates a Presigned PUT or GET URL without invoking external services.
     *
     * @param objectKey        The target S3 key (e.g., "uploads/my-file.zip")
     * @param httpMethod       HTTP verb ("PUT", "GET", "DELETE", etc.)
     * @param expirationSeconds Duration in seconds for which the URL remains valid
     * @param kvParams Additional key-value pairs to be included in the query string
     * @return Fully formatted presigned URL with SigV4 query parameters
     */
    public String generatePresignedS3Url(String objectKey, ConnectionMethod.HTTPMethod httpMethod, long expirationSeconds, String... kvParams) {
        if(kvParams.length % 2 != 0)
            throw new IllegalArgumentException("kvParams must be even number of arguments");

        CredentialsProvider.AwsCredentials credentials = credentialsProvider.get();

        Date date = new Date();
        String dateShort = AwsRequest.dateFormatShortThreadLocal.getValue().format(date);
        String dateIso = AwsRequest.dateFormatIsoThreadLocal.getValue().format(date);

        String host = bucket + ".s3." + region + ".amazonaws.com";
        String credentialScope = dateShort + "/" + region + "/s3/aws4_request";
        String fullCredential = credentials.AccessKeyId + "/" + credentialScope;

        // 1. Build Query Parameters (TreeMap guarantees alphabetical sorting required by SigV4)
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("X-Amz-Algorithm", "AWS4-HMAC-SHA256");
        queryParams.put("X-Amz-Credential", fullCredential);
        queryParams.put("X-Amz-Date", dateIso);
        queryParams.put("X-Amz-Expires", String.valueOf(expirationSeconds));
        queryParams.put("X-Amz-SignedHeaders", "host");
        for (int i = 0; i < kvParams.length; i += 2) {
            queryParams.put(kvParams[i], kvParams[i + 1]);
        }

        String canonicalQueryString = queryParams.entrySet().stream()
                .map(e -> AwsRequest.encode(e.getKey()) + "=" + AwsRequest.encode(e.getValue()))
                .collect(Collectors.joining("&"));

        // 2. Format Path and Canonical Request
        String canonicalPath = objectKey.startsWith("/") ? objectKey : "/" + objectKey;
        String canonicalHeaders = "host:" + host + "\n";
        String signedHeaders = "host";
        String payloadHash = "UNSIGNED-PAYLOAD";

        String canonicalRequest = String.join("\n",
                httpMethod.name(),
                canonicalPath,
                canonicalQueryString,
                canonicalHeaders,
                signedHeaders,
                payloadHash
        );

        // 3. Construct String to Sign
        String stringToSign = String.join("\n",
                "AWS4-HMAC-SHA256",
                dateIso,
                credentialScope,
                AwsRequest.getSha256(canonicalRequest)
        );

        // 4. Derive Signing Key & Compute Signature
        byte[] signingKey = AwsRequest.getSignatureKey(credentials.SecretAccessKey, dateShort, region, "s3");
        String signature = AwsRequest.toHexString(AwsRequest.hmacSHA256(stringToSign, signingKey), 64);

        // 5. Append Signature to Form Final URL
        return "https://" + host + canonicalPath + "?" + canonicalQueryString + "&X-Amz-Signature=" + signature;
    }
}
