/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import clusterless.cls.command.CommonCommandOptions;
import clusterless.cls.util.Moment;
import picocli.CommandLine;

import java.util.LinkedList;
import java.util.List;

public class DatasetStatusCommandOption extends CommonCommandOptions {
    @CommandLine.Mixin
    DatasetReportOptions datasetReportOptions = new DatasetReportOptions();

    @CommandLine.ArgGroup(
            exclusive = false,
            heading = "Time Range Options:%n"
    )
    RangeOptions rangeOptions = new RangeOptions();

    @CommandLine.Option(
            names = {"--name"},
            description = "Filter results by the name of the dataset."
    )
    List<String> names = new LinkedList<>();

    @CommandLine.Option(names = {"--list"}, description = {"List all dataset interval instances"})
    boolean list = false;

    public DatasetStatusCommandOption setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public boolean list() {
        return list;
    }

    public RangeOptions rangeOptions() {
        return rangeOptions;
    }

    public Moment earliest() {
        return rangeOptions.earliest();
    }

    public Moment latest() {
        return rangeOptions.latest();
    }


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

    public List<String> names() {
        return names;
    }
}
