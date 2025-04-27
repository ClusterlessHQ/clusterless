/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import clusterless.commons.temporal.IntervalUnits;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Optional;
import java.util.stream.Stream;

public class LotStream {
    public static Stream<String> stream(String earliestInclusive, String latestExclusive) {
        Optional<TemporalUnit> interval = IntervalUnits.findDurationWithin(earliestInclusive);
        Optional<Instant> first = IntervalUnitParser.parseIntervalUnit(earliestInclusive);
        Optional<Instant> second = IntervalUnitParser.parseIntervalUnit(latestExclusive);
        Instant begin = first.orElseThrow(() -> new IllegalArgumentException("Unable to parse into an instant: " + earliestInclusive));
        Instant end = second.orElseThrow(() -> new IllegalArgumentException("Unable to parse into an instant: " + latestExclusive));
        TemporalUnit intervalUnit = interval.orElseThrow(() -> new IllegalArgumentException("Unable to parse into an instant: " + earliestInclusive));

        DateTimeFormatter formatter = IntervalUnits.formatter(intervalUnit);

        return Stream.iterate(begin, i -> i.isBefore(end), i -> i.plus(1, intervalUnit))
                .map(formatter::format);
    }
}
