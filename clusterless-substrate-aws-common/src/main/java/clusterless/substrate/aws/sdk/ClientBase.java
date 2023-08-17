/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.sdk;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.awscore.internal.AwsErrorCode;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.InputStream;
import java.net.ConnectException;
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

    protected URI endpointOverride = Optional.ofNullable(System.getenv().get(getEndpointEnvVar()))
            .map(URI::create)
            .orElse(null);
    protected final DefaultCredentialsProvider credentialsProvider;
    protected final String region;

    public ClientBase() {
        this(defaultProfile, defaultRegion);
    }

    public ClientBase(String profile) {
        this.credentialsProvider = DefaultCredentialsProvider.builder()
                .profileName(profile)
                .build();
        this.region = defaultRegion;
    }

    public ClientBase(String profile, String region) {
        this.credentialsProvider = DefaultCredentialsProvider.builder()
                .profileName(profile == null ? defaultProfile : profile)
                .build();
        this.region = region == null ? defaultRegion : region;
    }

    @NotNull
    protected abstract String getEndpointEnvVar();

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
        return createClient(region);
    }

    protected abstract C createClient(String region);

    protected void logEndpointOverride() {
        if (endpointOverride != null) {
            LOG.info("{}: client using endpoint override: {}", getClass().getSimpleName(), endpointOverride);
        } else if (localStackEnabled) {
            LOG.warn("{}: client not using endpoint override", getClass().getSimpleName());
        }
    }

    public class Response {

        final AwsResponse awsResponse;
        final SdkHttpResponse sdkHttpResponse;
        Exception exception;
        ResponseBytes<GetObjectResponse> objectAsBytes;

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

        protected AwsResponse awsResponse() {
            return awsResponse;
        }

        protected SdkHttpResponse sdkHttpResponse() {
            return sdkHttpResponse;
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

        public boolean isAccessDenied() {
            if (exception instanceof AwsServiceException serviceException) {
                AwsErrorDetails awsErrorDetails = serviceException.awsErrorDetails();
                return awsErrorDetails.errorCode().equals("AccessDenied");
            }

            return false;
        }

        public boolean isThrottled() {
            if (exception instanceof AwsServiceException serviceException) {
                return serviceException.isThrottlingException();
            }

            return false;
        }

        public boolean isRetryable() {
            if (exception instanceof AwsServiceException serviceException) {
                return AwsErrorCode.isRetryableErrorCode(serviceException.awsErrorDetails().errorCode());
            }

            return false;
        }

        public GetObjectResponse asGetObjectResponse() {
            return objectAsBytes.response();
        }

        public ByteBuffer asByteBuffer() {
            return objectAsBytes.asByteBuffer();
        }

        public InputStream asInputStream() {
            return objectAsBytes.asInputStream();
        }

    }

    protected static void verifyResponse(ClientBase<S3Client>.Response response) {
        if (response.exception == null) {
            return;
        }

        if (Throwables.getRootCause(response.exception) instanceof ConnectException) {
            throw new IllegalStateException("unable to connect to S3", response.exception);
        }
    }
}
