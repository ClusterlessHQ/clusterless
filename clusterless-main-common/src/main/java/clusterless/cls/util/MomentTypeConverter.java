/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import clusterless.commons.temporal.IntervalUnit;
import clusterless.commons.temporal.IntervalUnits;
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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MomentTypeConverter implements CommandLine.ITypeConverter<Moment> {
    private static final Map<String, DateTimeFormatter> units = Arrays.stream(IntervalUnit.values())
            .collect(Collectors.toMap(u -> u.getDuration().toString(), IntervalUnits::formatter));

    private Supplier<ZoneId> zoneId = () -> ZoneOffset.UTC;

    RelativeDateTimeAdjusterParser adjusterParser;
    AbsoluteDateTimeParser absoluteParser;

    public MomentTypeConverter() {
        Context context = getContext();
        adjusterParser = new RelativeDateTimeAdjusterParser(context);
        absoluteParser = new AbsoluteDateTimeParser(context);
    }

    protected Context getContext() {
        return new Context(Clock.system(zoneId.get()));
    }

    @Override
    public Moment convert(String value) throws ParserSyntaxException {
        return new Moment(value, parse(value));
    }

    private Instant parse(String moment) throws ParserSyntaxException {
        Optional<Instant> first = units.entrySet()
                .stream()
                .filter(e -> moment.contains(e.getKey()))
                .map(e -> e.getValue().parse(moment).query(Instant::from))
                .findFirst();

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
