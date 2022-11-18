package com.wizzardo.tools.aws;

import com.wizzardo.tools.http.Request;

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
}
