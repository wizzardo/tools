package com.wizzardo.tools.aws;

import com.wizzardo.tools.json.JsonTools;
import com.wizzardo.tools.misc.Unchecked;

import java.util.Date;
import java.util.Optional;

import static com.wizzardo.tools.misc.With.with;


public interface CredentialsProvider {

    AwsCredentials get();

    class AwsCredentials {
        String Code;
        Date LastUpdated;
        Date Expiration;
        String Type;
        String AccessKeyId;
        String SecretAccessKey;
        String Token;
    }

    static CredentialsProvider createSimpleProvider(String keyId, String secret) {
        return () -> with(new AwsCredentials(), it -> {
            it.AccessKeyId = keyId;
            it.SecretAccessKey = secret;
        });
    }

    static Optional<CredentialsProvider> createFromEnvironmentVariables() {
        String aws_access_key_id = System.getenv().get("AWS_ACCESS_KEY_ID");
        String aws_secret_access_key = System.getenv().get("AWS_SECRET_ACCESS_KEY");
        if (aws_access_key_id == null || aws_access_key_id.isEmpty())
            return Optional.empty();
        if (aws_secret_access_key == null || aws_secret_access_key.isEmpty())
            return Optional.empty();

        return Optional.of(createSimpleProvider(aws_access_key_id, aws_secret_access_key));
    }

    static Optional<CredentialsProvider> createFromMetaData() {
        return Unchecked.ignore(() -> {
            String role = EC2MetaData.get("/iam/security-credentials/");
            String[] rolePath = role.split("\\n", 2);
            if (rolePath.length == 0)
                throw new IllegalStateException("Can parse role: " + role);
            String json = EC2MetaData.get("/iam/security-credentials/" + rolePath[0]);
            AwsCredentials credentials = JsonTools.parse(json, AwsCredentials.class);
            return Optional.of(() -> credentials);
        }, Optional.empty());
    }

    /**
     * @return CredentialsProvider created from environment variables or EC2 metadata
     */
    static CredentialsProvider create() {
        return createFromEnvironmentVariables()
                .orElseGet(() -> createFromMetaData().orElse(null));
    }
}
