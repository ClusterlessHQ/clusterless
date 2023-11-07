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

@CommandLine.Command(
        name = "status",
        description = "Show the status of the current target."
)
public class ArcStatusCommandOption extends CommonCommandOptions {
    @CommandLine.Mixin
    ArcReportOptions arcReportOptions = new ArcReportOptions();

    @CommandLine.Mixin
    RangeOptions rangeOptions = new RangeOptions();

    @CommandLine.Option(
            names = {"--name"},
            description = "Filter results by the name of the target to show."
    )
    List<String> names = new LinkedList<>();

    @CommandLine.Option(names = {"--list"}, description = {"List all arc instances"})
    boolean list = false;

    public ArcStatusCommandOption setNames(List<String> names) {
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

    public ArcReportOptions setProjects(List<String> projects) {
        return arcReportOptions.setProjects(projects);
    }

    public ReportOptions setProfile(String profile) {
        return arcReportOptions.setProfile(profile);
    }

    public ReportOptions setAccount(String account) {
        return arcReportOptions.setAccount(account);
    }

    public ReportOptions setRegion(String region) {
        return arcReportOptions.setRegion(region);
    }

    public ReportOptions setStage(String stage) {
        return arcReportOptions.setStage(stage);
    }

    public List<String> projects() {
        return arcReportOptions.projects();
    }

    public String profile() {
        return arcReportOptions.profile();
    }

    public String account() {
        return arcReportOptions.account();
    }

    public String region() {
        return arcReportOptions.region();
    }

    public String stage() {
        return arcReportOptions.stage();
    }

    public List<String> names() {
        return names;
    }
}
