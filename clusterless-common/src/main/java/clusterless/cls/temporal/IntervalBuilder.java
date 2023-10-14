/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.temporal;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;

/**
 *
 */
public class IntervalBuilder {
    final TemporalUnit lotUnit;
    final DateTimeFormatter lotFormatter;

    public IntervalBuilder(TemporalUnit lotUnit) {
        this.lotUnit = lotUnit;
        this.lotFormatter = IntervalUnits.formatter(this.lotUnit);
    }

    public IntervalBuilder(String lotUnit) {
        this.lotUnit = IntervalUnits.find(lotUnit);
        this.lotFormatter = IntervalUnits.formatter(this.lotUnit);
    }

    public String truncateAndFormat(OffsetDateTime time) {
        return format(truncate(time));
    }

    public String truncateAndFormat(Instant instant) {
        return format(truncate(instant));
    }

    @NotNull
    public String format(OffsetDateTime truncate) {
        return lotFormatter.format(truncate);
    }

    @NotNull
    public String format(Instant truncate) {
        return lotFormatter.format(truncate);
    }

    @NotNull
    public OffsetDateTime truncate(OffsetDateTime time) {
        return time.truncatedTo(lotUnit);
    }

    @NotNull
    public Instant truncate(Instant instant) {
        return instant.truncatedTo(lotUnit);
    }

    public OffsetDateTime previous(OffsetDateTime time) {
        return time.minus(lotUnit.getDuration());
    }

    public Instant previous(Instant instant) {
        return instant.minus(lotUnit.getDuration());
    }
}
