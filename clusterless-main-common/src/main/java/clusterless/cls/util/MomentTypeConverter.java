/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import heretical.parser.common.ParserSyntaxException;
import heretical.parser.temporal.AbsoluteDateTimeParser;
import heretical.parser.temporal.Context;
import heretical.parser.temporal.RelativeDateTimeAdjusterParser;
import heretical.parser.temporal.TemporalResult;
import heretical.parser.temporal.expression.AdjusterExp;
import heretical.parser.temporal.expression.DateTimeExp;
import picocli.CommandLine;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

public class MomentTypeConverter implements CommandLine.ITypeConverter<Moment> {
    /**
     * Locks the clock to now for the lifetime of the jvm so that earliest now and latest now are the same.
     */
    private static final Lazy<Clock> clockLazy = Lazy.of(() -> Clock.fixed(Clock.system(ZoneOffset.UTC).instant(), ZoneOffset.UTC));

    RelativeDateTimeAdjusterParser adjusterParser;
    AbsoluteDateTimeParser absoluteParser;

    public MomentTypeConverter() {
        Context context = getContext();
        adjusterParser = new RelativeDateTimeAdjusterParser(context);
        absoluteParser = new AbsoluteDateTimeParser(context);
    }

    protected Context getContext() {
        return new Context(clockLazy.get());
    }

    @Override
    public Moment convert(String value) throws ParserSyntaxException {
        return new Moment(value, parse(value));
    }

    private Instant parse(String moment) throws ParserSyntaxException {
        Optional<Instant> first = IntervalUnitParser.parseIntervalUnit(moment);

        if (first.isPresent()) {
            return first.get();
        }

        TemporalResult<DateTimeExp, Instant> instant = absoluteParser.parse(moment);

        if (instant.matched()) {
            return instant.getResult();
        }

        TemporalResult<AdjusterExp, Instant> adjuster = adjusterParser.parse(moment);

        if (adjuster.matched()) {
            return adjuster.getResult();
        }

        if (instant.getErrorStartIndex(0) == 0) {
            throw new ParserSyntaxException(adjuster);
        }

        throw new ParserSyntaxException(instant);
    }
}
