/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.sdk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 */
public abstract class ClientBase<C> {
    private static final Logger LOG = LogManager.getLogger(ClientBase.class);
    protected static final boolean localStackEnabled = Boolean.getBoolean("clusterless.localstack.enabled");
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

    protected void logEndpointOverride() {
        if (endpointOverride != null) {
            LOG.info("client using endpoint override: {}", endpointOverride);
        } else if (localStackEnabled) {
            LOG.warn("s3 client not using endpoint override");
        }
    }

    public class Response {
        final AwsResponse awsResponse;
        final SdkHttpResponse sdkHttpResponse;
        Exception exception;
        ResponseBytes<GetObjectResponse> objectAsBytes;

        public Response(PutEventsResponse putEventsResponse) {
            this((AwsResponse) putEventsResponse);
        }

        public Response(AwsResponse awsResponse) {
            this.awsResponse = awsResponse;
            this.sdkHttpResponse = awsResponse.sdkHttpResponse();
        }

        public Response(Exception exception) {
            this.awsResponse = null;
            this.sdkHttpResponse = null;
            this.exception = exception;
        }

        public Response(ResponseBytes<GetObjectResponse> objectAsBytes) {
            this.awsResponse = objectAsBytes.response();
            this.sdkHttpResponse = this.awsResponse.sdkHttpResponse();
            this.objectAsBytes = objectAsBytes;
        }

        public boolean isSuccess() {
            return ClientBase.this.isSuccess(this);
        }

        public void isSuccessOrThrowRuntime() {
            isSuccessOrThrow(RuntimeException::new);
        }

        public void isSuccessOrThrow(Function<Exception, RuntimeException> exception) {
            if (isSuccess()) {
                return;
            }

            LOG.error(this.errorMessage());

            throw exception.apply(this.exception);
        }

        public void isSuccessOrThrowRuntime(Function<Response, String> message) {
            isSuccessOrThrow(message, RuntimeException::new);
        }

        public void isNotSuccessOrThrow(Function<Response, String> message, BiFunction<String, Exception, RuntimeException> exception) {
            isOrThrow(Predicate.not(Response::isSuccess), message, exception);
        }

        public void isSuccessOrThrow(Function<Response, String> message, BiFunction<String, Exception, RuntimeException> exception) {
            isOrThrow(Response::isSuccess, message, exception);
        }

        private void isOrThrow(Predicate<Response> predicate, Function<Response, String> message, BiFunction<String, Exception, RuntimeException> exception) {
            if (predicate.test(this)) {
                return;
            }

            String m = message.apply(this);
            LOG.error(m, this.errorMessage());

            throw exception.apply(m, this.exception);
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

        public InputStream inputStream() {
            return objectAsBytes.asInputStream();
        }
    }
}
