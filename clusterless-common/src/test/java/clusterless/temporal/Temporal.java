/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.temporal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class Temporal {

    /**
     * 96 fourths (0-95)
     * 144 sixths (0-143)
     * 288 twelfths (0-287)
     */
    private static Stream<Arguments> arguments() {
        return Stream.of(
                // Mon Feb 06 2023 23:52:06 GMT+0000
                Arguments.of(1675727526500L, 1675727100000L, 95, IntervalField.FOURTH_OF_DAY, "PT15M"),
                Arguments.of(1675727526500L, 1675727400000L, 143, IntervalField.SIXTH_OF_DAY, "PT10M"),
                Arguments.of(1675727526500L, 1675727400000L, 286, IntervalField.TWELFTH_OF_DAY, "PT5M"),
                // Mon Feb 06 2023 23:00:21 GMT+0000
                Arguments.of(1675724421500L, 1675724400000L, 96 - 4, IntervalField.FOURTH_OF_DAY, "PT15M"),
                Arguments.of(1675724421500L, 1675724400000L, 144 - 6, IntervalField.SIXTH_OF_DAY, "PT10M"),
                Arguments.of(1675724421500L, 1675724400000L, 288 - 12, IntervalField.TWELFTH_OF_DAY, "PT5M")
        );
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void test(long epochMilli, long truncatedMilli, int expected, IntervalField intervalField, String durationFragment) {
        TemporalUnit durationUnit = intervalField.getBaseUnit();
        DateTimeFormatter formatter = IntervalUnits.formatter(durationUnit);
        Instant instant = Instant.ofEpochMilli(epochMilli);

        assertEquals(truncatedMilli, instant.truncatedTo(durationUnit).toEpochMilli());

        int i = instant.get(intervalField);

        assertEquals(expected, i);

        String format = formatter.format(instant);

        assertEquals("20230206%s%03d".formatted(durationFragment, i), format);

        TemporalAccessor temporalAccessor = formatter.parse(format);

        LocalDateTime dateTime = temporalAccessor.query(LocalDateTime::from);

        assertEquals(LocalDateTime.ofInstant(instant.truncatedTo(durationUnit), ZoneId.of("UTC")), dateTime);
    }
}
