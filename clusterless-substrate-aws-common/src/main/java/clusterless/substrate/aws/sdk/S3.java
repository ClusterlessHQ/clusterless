/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.sdk;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public class S3 {

    private Optional<String> awsS3Endpoint;

    public class Response {

        SdkHttpResponse response;
        S3Exception exception;

        public Response(SdkHttpResponse response) {
            this.response = response;
        }

        public Response(S3Exception exception) {
            this.exception = exception;
        }
    }

    private String defaultRegion = System.getenv("AWS_DEFAULT_REGION");
    private String profile = System.getenv("AWS_PROFILE");

    public S3(String profile) {
        this.profile = profile;
    }

    public S3() {
    }

    public Response exists(String bucketName) {
        return exists(defaultRegion, bucketName);
    }

    public Response exists(String region, String bucketName) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(bucketName, "bucketName");

        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder()
                .profileName(profile)
                .build();

        HeadBucketResponse response;

        awsS3Endpoint = Optional.ofNullable(System.getenv().get("AWS_S3_ENDPOINT"));
        try (S3Client client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(
                        awsS3Endpoint
                                .map(URI::create).orElse(null)
                )
                .build()) {

            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            response = client.headBucket(request);
        } catch (NoSuchBucketException exception) {
            return new Response(exception);
        }

        return new Response(response.sdkHttpResponse());
    }

    public boolean isSuccess(Response response) {
        return response.exception == null && response.response.isSuccessful();
    }

    public String error(Response response) {
        if (response.exception != null) {
            return response.exception.getLocalizedMessage();
        }
        return response.response.statusText().orElse("code: %d".formatted(response.response.statusCode()));
    }
}
