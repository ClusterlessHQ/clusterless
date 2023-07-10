/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda;

import clusterless.temporal.IntervalBuilder;
import clusterless.temporal.IntervalUnit;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class TestLots {
    public static final String COMMON_LOT = "20211112PT5M000";

    protected IntervalUnit intervalUnit = IntervalUnit.TWELFTHS;
    protected final IntervalBuilder intervalBuilder = new IntervalBuilder(intervalUnit);
    private final OffsetDateTime startDateTime = OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    public TestLots() {
    }

    public TestLots(IntervalUnit intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Stream<String> lotStream(int size) {
        return LongStream.range(0, size)
                .map(i -> i * intervalUnit.getDuration().toMinutes())
                .boxed()
                .map(startDateTime::plusMinutes)
                .map(intervalBuilder::truncateAndFormat);
    }

    public List<String> lots(int size) {
        return lotStream(size)
                .collect(Collectors.toList());
    }

    public String format(OffsetDateTime dateTime) {
        return intervalBuilder.truncateAndFormat(dateTime);
    }
}
