/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.sdk;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 *
 */
public abstract class ClientBase<C> {
    protected static final String defaultRegion = System.getenv("AWS_DEFAULT_REGION");
    protected static final String defaultProfile = System.getenv("AWS_PROFILE");
    protected final DefaultCredentialsProvider credentialsProvider;
    protected URI endpointOverride = Optional.ofNullable(System.getenv().get("AWS_S3_ENDPOINT"))
            .map(URI::create).orElse(null);

    public ClientBase() {
        this(defaultProfile);
    }

    public ClientBase(String profile) {
        credentialsProvider = DefaultCredentialsProvider.builder()
                .profileName(profile)
                .build();
    }

    protected boolean isSuccess(Response response) {
        return response.exception == null && response.sdkHttpResponse.isSuccessful();
    }

    public String error(Response response) {
        if (response.exception != null) {
            return response.exception.getLocalizedMessage();
        }
        return response.sdkHttpResponse.statusText().orElse(String.format("status code: %d", response.sdkHttpResponse.statusCode()));
    }

    protected C createClient() {
        return createClient(defaultRegion);
    }

    protected abstract C createClient(String region);

    public class Response {

        AwsResponse awsResponse;
        SdkHttpResponse sdkHttpResponse;
        Exception exception;
        ResponseBytes<GetObjectResponse> objectAsBytes;

        public Response(PutEventsResponse putEventsResponse) {
            this((AwsResponse) putEventsResponse);
        }

        public Response(AwsResponse awsResponse) {
            this.awsResponse = awsResponse;
            this.sdkHttpResponse = awsResponse.sdkHttpResponse();
        }

        public Response(SdkHttpResponse sdkHttpResponse) {
            this.sdkHttpResponse = sdkHttpResponse;
        }

        public Response(Exception exception) {
            this.exception = exception;
        }

        public Response(ResponseBytes<GetObjectResponse> objectAsBytes) {
            this.awsResponse = objectAsBytes.response();
            this.objectAsBytes = objectAsBytes;
        }

        public boolean isSuccess() {
            return ClientBase.this.isSuccess(this);
        }

        public String errorMessage() {
            if (exception != null) {
                return exception.getMessage();
            }

            return null;
        }

        public Exception exception() {
            return exception;
        }

        public ByteBuffer objectAsBytes() {
            return objectAsBytes.asByteBuffer();
        }
    }
}
