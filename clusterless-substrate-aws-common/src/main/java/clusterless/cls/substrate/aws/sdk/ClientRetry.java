/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.sdk;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.AwsClient;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ClientRetry<C extends AwsClient> {
    private static final Logger LOG = LoggerFactory.getLogger(ClientRetry.class);
    private final RetryConfig config;
    private final String client;

    public ClientRetry(String client, int maxAttempts, Predicate<ClientBase<C>.Response> predicate) {
        this.client = client;
        this.config = RetryConfig.<ClientBase<C>.Response>custom()
                .maxAttempts(maxAttempts)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(30), 2.0, Duration.ofMinutes(5)))
                .consumeResultBeforeRetryAttempt((attempt, response) -> LOG.warn("got: {}, for retry attempt: {} of {}", response.errorMessage(), attempt, maxAttempts))
                .retryOnResult(predicate)
                .failAfterMaxAttempts(true)
                .build();
    }

    public ClientBase<C>.Response invoke(Supplier<ClientBase<C>.Response> checkedSupplier) {
        return Retry.of(client, config)
                .executeSupplier(checkedSupplier);
    }
}
