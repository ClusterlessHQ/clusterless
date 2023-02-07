/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.temporal;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

import static java.time.temporal.ChronoField.MINUTE_OF_DAY;
import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * Breaks a day into the number of intervals requested.
 * <p>
 * Fourths is a 15-minute duration, there are 4 Fourths in an hour, and 96 Fourths in a day.
 * <p>
 * Sixths is a 10-minute duration, there are 6 Sixths in an hour, and 144 Sixths in a day.
 * <p>
 * Twelfths is a 5-minute duration, there are 12 Twelfths in an hour, and 288 Twelfth in a day.
 */
public enum IntervalUnit implements TemporalUnit {
    FOURTHS("Fourths", Duration.ofMinutes(15)),
    SIXTHS("Sixths", Duration.ofMinutes(10)),
    TWELFTHS("Twelfths", Duration.ofMinutes(5));

    private final String name;
    private final Duration duration;

    IntervalUnit(String name, Duration estimatedDuration) {
        this.name = name;
        this.duration = estimatedDuration;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean isDurationEstimated() {
        return false;
    }

    @Override
    public boolean isDateBased() {
        return false;
    }

    @Override
    public boolean isTimeBased() {
        return true;
    }

    @Override
    public boolean isSupportedBy(Temporal temporal) {
        return temporal.isSupported(MINUTE_OF_DAY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Temporal> R addTo(R temporal, long amount) {
        return switch (this) {
            case FOURTHS -> (R) temporal.plus(15 * amount, MINUTES);
            case SIXTHS -> (R) temporal.plus(10 * amount, MINUTES);
            case TWELFTHS -> (R) temporal.plus(5 * amount, MINUTES);
        };
    }

    @Override
    public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
        if (temporal1Inclusive.getClass() != temporal2Exclusive.getClass()) {
            return temporal1Inclusive.until(temporal2Exclusive, this);
        }
        return switch (this) {
            case FOURTHS -> temporal1Inclusive.until(temporal2Exclusive, MINUTES) / 15;
            case SIXTHS -> temporal1Inclusive.until(temporal2Exclusive, MINUTES) / 10;
            case TWELFTHS -> temporal1Inclusive.until(temporal2Exclusive, MINUTES) / 5;
        };
    }

    @Override
    public String toString() {
        return name;
    }
}
