/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.temporal;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static clusterless.cls.temporal.IntervalField.*;
import static java.time.temporal.ChronoField.*;

/**
 *
 */
public class IntervalDateTimeFormatter {
    public static final DateTimeFormatter FOURTH_FORMATTER;
    public static final DateTimeFormatter SIXTH_FORMATTER;
    public static final DateTimeFormatter TWELFTH_FORMATTER;

    static {
        FOURTH_FORMATTER = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral(FOURTH_OF_DAY.getBaseUnit().getDuration().toString())
                .appendValue(FOURTH_OF_DAY, 3)
                .toFormatter()
                .withZone(ZoneOffset.UTC);
    }

    static {
        SIXTH_FORMATTER = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral(SIXTH_OF_DAY.getBaseUnit().getDuration().toString())
                .appendValue(SIXTH_OF_DAY, 3)
                .toFormatter()
                .withZone(ZoneOffset.UTC);
    }

    static {
        TWELFTH_FORMATTER = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral(TWELFTH_OF_DAY.getBaseUnit().getDuration().toString())
                .appendValue(TWELFTH_OF_DAY, 3)
                .toFormatter()
                .withZone(ZoneOffset.UTC);
    }
}
