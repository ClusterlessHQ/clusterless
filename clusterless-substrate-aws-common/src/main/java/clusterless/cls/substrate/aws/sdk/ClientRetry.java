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
import org.jetbrains.annotations.NotNull;
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

    @NotNull
    public static IntervalFunction exponentialBackoff(Duration initialInterval, double multiplier, Duration maxInterval) {
        return IntervalFunction.ofExponentialBackoff(initialInterval, multiplier, maxInterval);
    }

    public ClientRetry(String client, int maxAttempts, Duration fixed, Predicate<ClientBase<C>.Response> predicate) {
        this(client, maxAttempts, IntervalFunction.of(fixed), predicate);
    }

    public ClientRetry(String client, int maxAttempts, Predicate<ClientBase<C>.Response> predicate) {
        this(client, maxAttempts, exponentialBackoff(Duration.ofSeconds(30), 2.0, Duration.ofMinutes(5)), predicate);
    }

    public ClientRetry(String client, Duration maxDuration, IntervalFunction function, Predicate<ClientBase<C>.Response> predicate) {
        this(client, maxAttempts(maxDuration, function), function, predicate);
    }

    protected static int maxAttempts(Duration maxDuration, IntervalFunction function) {
        Duration duration = Duration.ZERO;

        int count = 0;
        while (duration.toMillis() < maxDuration.toMillis()) {
            count++;
            duration = duration.plus(Duration.ofMillis(function.apply(count)));
        }

        if (count == 0) {
            LOG.warn("calculated max attempts are zero, for maxDuration: {}, returning value of 1 max attempts", maxDuration);
            return 1;
        }

        return count;
    }

    public ClientRetry(String client, int maxAttempts, IntervalFunction function, Predicate<ClientBase<C>.Response> predicate) {
        this.client = client;
        this.config = RetryConfig.<ClientBase<C>.Response>custom()
                .maxAttempts(maxAttempts)
                .intervalFunction(function)
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
