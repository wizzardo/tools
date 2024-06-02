package com.wizzardo.tools.aws;

import com.wizzardo.tools.http.Request;
import com.wizzardo.tools.http.Response;

import java.io.IOException;

public class EC2MetaData {

    public static String get(String path) throws IOException {
        return get(path, 3000);
    }

    public static String get(String path, int timeout) throws IOException {
        String url = "http://169.254.169.254/latest/meta-data" + path;
        Response response = new Request(url)
                .timeout(timeout)
                .get();

        if (response.getResponseCode() == 401) {
            String token = new Request("http://169.254.169.254/latest/api/token")
                    .header("X-aws-ec2-metadata-token-ttl-seconds", "60")
                    .timeout(timeout)
                    .get()
                    .asString();

            response = new Request(url)
                    .header("X-aws-ec2-metadata-token", token)
                    .timeout(timeout)
                    .get();
        }

        return response.asString();
    }
}
