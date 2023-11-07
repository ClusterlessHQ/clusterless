/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import java.time.Instant;

public class Moment {

    public static Moment now() {
        return new Moment("now", Instant.now());
    }

    public static Moment parse(String moment) {
        return null;
    }

    private final String moment;

    private final Instant instant;

    public Moment(String moment, Instant instant) {
        this.moment = moment;
        this.instant = instant;
    }

    public String moment() {
        return moment;
    }

    public Instant instant() {
        return instant;
    }

    public boolean isNow() {
        return moment.equals("now");
    }

    public String toInstantEpochSecondsString() {
        return Long.toString(instant.getEpochSecond());
    }

    public String toInstantEpochMillisString() {
        return Long.toString(instant.toEpochMilli());
    }

    @Override
    public String toString() {
        return moment;
    }
}
