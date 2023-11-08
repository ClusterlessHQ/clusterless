/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import clusterless.cls.util.Moment;
import clusterless.cls.util.MomentTypeConverter;
import picocli.CommandLine;

public class RangeOptions {
    @CommandLine.Option(
            names = {"--earliest"},
            description = {
                    "Earliest time to include in report, inclusive.",
                    "Where the time can be most any date/time format, or",
                    "an adjuster such as '1h' or '1d'.",
                    "(default: ${DEFAULT-VALUE})"
            },
            converter = MomentTypeConverter.class,
            defaultValue = "12h"
    )
    private Moment earliest;
    @CommandLine.Option(
            names = {"--latest"},
            description = {
                    "Latest time to include in report, exclusive.",
                    "Where the time can be most any date/time format, or",
                    "an adjuster such as '1h' or '1d'.",
                    "(default: ${DEFAULT-VALUE})"
            },
            converter = MomentTypeConverter.class
    )
    private Moment latest = Moment.now();

    public Moment earliest() {
        return earliest;
    }

    public Moment latest() {
        return latest;
    }
}
