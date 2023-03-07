/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.sdk;

import clusterless.json.JSONUtil;
import clusterless.util.URIs;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

/**
 *
 */
public class S3 extends ClientBase<S3Client> {

    public S3() {
    }

    public S3(String profile) {
        super(profile);
    }

    public Response exists(String bucketName) {
        return exists(defaultRegion, bucketName);
    }

    public Response exists(String region, String bucketName) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(bucketName, "bucketName");

        HeadBucketRequest request = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

        try (S3Client client = createClient(region)) {
            return new Response(client.headBucket(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response exists(URI location) {
        return exists(defaultRegion, location);
    }

    public Response exists(String region, URI location) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(location, "location");

        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(location.getHost())
                .key(URIs.asKeyPath(location))
                .build();

        try (S3Client client = createClient(region)) {
            return new Response(client.headObject(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response create(String bucketName) {
        return create(defaultRegion, bucketName);
    }

    public Response create(String region, String bucketName) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(bucketName, "bucketName");

        CreateBucketRequest request = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

        try (S3Client client = createClient(region)) {
            return new Response(client.createBucket(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response put(URI location, String contentType, Object value) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(value, "value");

        String bucketName = location.getHost();
        String key = URIs.asKeyPath(location);
        String body = JSONUtil.writeAsStringSafe(value);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        try (S3Client client = createClient()) {
            return new Response(client.putObject(putObjectRequest, RequestBody.fromString(body)));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response get(URI location) {
        Objects.requireNonNull(location, "location");

        String bucketName = location.getHost();
        String key = URIs.asKeyPath(location);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (S3Client client = createClient()) {
            return new Response(client.getObjectAsBytes(getObjectRequest));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    /**
     * Has a limit of 5G objects,
     *
     * @param from
     * @param to
     * @return
     */
    public Response copy(URI from, URI to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");

        String fromBucket = from.getHost();
        String fromKey = URIs.asKeyPath(from);

        String toBucket = to.getHost();
        String toKey = URIs.asKeyPath(to);

        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                .sourceBucket(fromBucket)
                .sourceKey(fromKey)
                .destinationBucket(toBucket)
                .destinationKey(toKey)
                .build();

        try (S3Client client = createClient()) {
            return new Response(client.copyObject(copyObjectRequest));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public boolean exists(Response response) {
        if (response.exception instanceof NoSuchBucketException || response.exception instanceof NoSuchKeyException) {
            return false;
        }

        if (response.exception != null) {
            return false;
        }

        return response.sdkHttpResponse.isSuccessful();
    }

    public Instant lastModified(Response response) {
        return ((HeadObjectResponse) response.awsResponse).lastModified();
    }

    @Override
    protected S3Client createClient(String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }
}
