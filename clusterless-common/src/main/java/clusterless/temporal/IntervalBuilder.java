/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.temporal;

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

    public IntervalBuilder(String lotUnit) {
        this.lotUnit = IntervalUnits.find(lotUnit);
        this.lotFormatter = IntervalUnits.formatter(this.lotUnit);
    }

    public String truncateAndFormat(OffsetDateTime time) {
        return lotFormatter.format(time.truncatedTo(lotUnit));
    }

    public String truncateAndFormat(Instant instant) {
        return lotFormatter.format(instant.truncatedTo(lotUnit));
    }
}
