/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import picocli.CommandLine;

public class DatasetsCommandOptions extends ReportCommandOptions {
    @CommandLine.Mixin
    DatasetReportOptions datasetReportOptions = new DatasetReportOptions();

    public ReportOptions setProfile(String profile) {
        return datasetReportOptions.setProfile(profile);
    }

    public ReportOptions setAccount(String account) {
        return datasetReportOptions.setAccount(account);
    }

    public ReportOptions setRegion(String region) {
        return datasetReportOptions.setRegion(region);
    }

    public ReportOptions setStage(String stage) {
        return datasetReportOptions.setStage(stage);
    }

    public String profile() {
        return datasetReportOptions.profile();
    }

    public String account() {
        return datasetReportOptions.account();
    }

    public String region() {
        return datasetReportOptions.region();
    }

    public String stage() {
        return datasetReportOptions.stage();
    }
}
