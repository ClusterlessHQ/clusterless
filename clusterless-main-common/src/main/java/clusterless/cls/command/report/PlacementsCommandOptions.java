/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import picocli.CommandLine;

public class PlacementsCommandOptions extends ReportCommandOptions {
    @CommandLine.Mixin
    ReportOptions reportOptions = new ReportOptions();

    @Override
    public String profile() {
        return reportOptions.profile();
    }

    @Override
    public String account() {
        return reportOptions.account();
    }

    @Override
    public String region() {
        return reportOptions.region();
    }

    @Override
    public String stage() {
        return reportOptions.stage();
    }
}
