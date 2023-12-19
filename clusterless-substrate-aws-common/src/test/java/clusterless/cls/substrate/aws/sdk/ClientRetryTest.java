/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.sdk;

import io.github.resilience4j.core.IntervalFunction;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientRetryTest {
    @Test
    void max() {
        assertEquals(2, ClientRetry.maxAttempts(Duration.ofSeconds(60), IntervalFunction.of(Duration.ofSeconds(30))));
        assertEquals(2, ClientRetry.maxAttempts(Duration.ofSeconds(60), IntervalFunction.ofExponentialBackoff(30000, 2, 600000)));

        IntervalFunction intervalFunction = IntervalFunction.ofExponentialBackoff(15000, 2, 600000);
//        System.out.println("IntStream.range(1,3).mapToLong(intervalFunction::apply).sum() = " + IntStream.range(1, 3).mapToLong(intervalFunction::apply).peek(System.out::println).sum());
        assertEquals(3, ClientRetry.maxAttempts(Duration.ofSeconds(60), intervalFunction));

        IntervalFunction intervalFunction1 = IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(3), 2, Duration.ofMinutes(1));
//        System.out.println("IntStream.range(1,19).mapToLong(intervalFunction::apply).sum() = " + IntStream.range(1, 19).mapToLong(intervalFunction1::apply).peek(System.out::println).sum());
//        System.out.println("Duration.ofMinutes(15).toMillis() = " + Duration.ofMinutes(15).toMillis());
        assertEquals(19, ClientRetry.maxAttempts(Duration.ofMinutes(15), intervalFunction1));
    }
}
