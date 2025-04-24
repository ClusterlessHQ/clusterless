/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import clusterless.commons.temporal.IntervalUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class IntervalUnitParser {
    private static final Map<String, DateTimeFormatter> units = Arrays.stream(IntervalUnit.values())
            .collect(Collectors.toMap(u -> u.getDuration().toString(), clusterless.commons.temporal.IntervalUnits::formatter));

    public static Moment convert(String value) {
        Optional<Instant> instant = parseIntervalUnit(value);
        return new Moment(value, instant.orElseThrow(() -> new IllegalArgumentException("Unable to parse into an instant: " + value)));
    }

    static @NotNull Optional<Instant> parseIntervalUnit(String moment) {
        return units.entrySet()
                .stream()
                .filter(e -> moment.contains(e.getKey()))
                .map(e -> e.getValue().parse(moment).query(Instant::from))
                .findFirst();
    }
}
