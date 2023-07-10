/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.sdk;

import clusterless.json.JSONUtil;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.stream.Collectors;

import static software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName.SENT_TIMESTAMP;

public class SQS extends ClientBase<SqsClient> {
    public SQS() {
    }

    @NotNull
    protected String getEndpointEnvVar() {
        return "AWS_SQS_ENDPOINT";
    }

    @Override
    protected SqsClient createClient(String region) {
        logEndpointOverride();

        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }

    public Response create(String queueName) {
        CreateQueueRequest request = CreateQueueRequest.builder()
                .queueName(queueName)
                .build();

        try (SqsClient sqsClient = createClient()) {
            return new Response(sqsClient.createQueue(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response queueUrl(String queueName) {
        try (SqsClient client = createClient()) {
            GetQueueUrlRequest queueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
            return new Response(client.getQueueUrl(queueUrlRequest));
        }
    }

    public String queueUrl(Response response) {
        return ((GetQueueUrlResponse) response.awsResponse()).queueUrl();
    }

    public Response put(String queueUrl, Object source) {
        return put(queueUrl, JSONUtil.writeAsStringSafe(source));
    }

    public Response put(String queueUrl, String source) {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(source)
                .build();

        try (SqsClient sqsClient = createClient()) {
            return new Response(sqsClient.sendMessage(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response get(String queueUrl, int waitTimeSeconds) {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .waitTimeSeconds(waitTimeSeconds)
                .attributeNamesWithStrings(SENT_TIMESTAMP.toString())
                .maxNumberOfMessages(10) // the maximum allowed by SQS
                .queueUrl(queueUrl)
                .build();

        try (SqsClient sqsClient = createClient()) {
            return new Response(sqsClient.receiveMessage(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response delete(String queueUrl, List<String> receiptHandles) {
        if (receiptHandles.isEmpty()) {
            throw new IllegalArgumentException("receiptHandles cannot be empty");
        }
        final int[] count = {0};
        long currentTimeMillis = System.currentTimeMillis();

        List<DeleteMessageBatchRequestEntry> entries = receiptHandles.stream()
                .map(s -> DeleteMessageBatchRequestEntry.builder()
                        .id(String.format("%d-%d", currentTimeMillis, count[0]++))
                        .receiptHandle(s)
                        .build())
                .collect(Collectors.toList());

        DeleteMessageBatchRequest request = DeleteMessageBatchRequest.builder()
                .queueUrl(queueUrl)
                .entries(entries)
                .build();

        try (SqsClient sqsClient = createClient()) {
            return new Response(sqsClient.deleteMessageBatch(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public List<Message> get(Response response) {
        return ((ReceiveMessageResponse) response.awsResponse()).messages();
    }
}
