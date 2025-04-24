/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.exec;

import clusterless.cls.command.CommonCommandOptions;
import clusterless.cls.command.common.CommonOptions;
import clusterless.cls.command.common.RangeOptions;
import clusterless.cls.command.entity.ArcCommonOptions;
import clusterless.cls.model.state.ArcState;
import clusterless.cls.util.Moment;
import picocli.CommandLine;

import java.util.LinkedList;
import java.util.List;

public class ArcExecCommandOptions extends CommonCommandOptions {
    @CommandLine.Mixin
    ArcCommonOptions arcCommonOptions = new ArcCommonOptions();

    static class RangeOrLot {
        @CommandLine.ArgGroup(
                exclusive = false,
                heading = "Time Range Options:%n"
        )
        RangeOptions rangeOptions = new RangeOptions();

        @CommandLine.Option(
                names = {"--lot"},
                split = ",",
                description = "Apply to only these lots."
        )
        List<String> lots = new LinkedList<>();
    }

    @CommandLine.ArgGroup(
            heading = "Time Range or Lot Options:%n"
    )
    RangeOrLot rangeOrLot = new RangeOrLot();

    @CommandLine.Option(
            names = {"--name"},
            split = ",",
            description = "Filter results by the name of the arc."
    )
    List<String> names = new LinkedList<>();

    @CommandLine.Option(
            names = {"--state"},
            split = ",",
            description = "Filter results by the state of the arc."
    )
    List<ArcState> states = new LinkedList<>();

    @CommandLine.Option(
            names = {"--source"},
            split = ",",
            description = "Execute against this given source."
    )
    List<String> sources = new LinkedList<>();

    public ArcExecCommandOptions setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public RangeOptions rangeOptions() {
        return rangeOrLot.rangeOptions;
    }

    public Moment earliest() {
        return rangeOrLot.rangeOptions.earliest();
    }

    public Moment latest() {
        return rangeOrLot.rangeOptions.latest();
    }

    public ArcCommonOptions arcCommonOptions() {
        return arcCommonOptions;
    }

    public ArcCommonOptions setProjects(List<String> projects) {
        return arcCommonOptions.setProjects(projects);
    }

    public CommonOptions setProfile(String profile) {
        return arcCommonOptions.setProfile(profile);
    }

    public CommonOptions setAccount(String account) {
        return arcCommonOptions.setAccount(account);
    }

    public CommonOptions setRegion(String region) {
        return arcCommonOptions.setRegion(region);
    }

    public CommonOptions setStage(String stage) {
        return arcCommonOptions.setStage(stage);
    }

    public List<String> projects() {
        return arcCommonOptions.projects();
    }

    public String profile() {
        return arcCommonOptions.profile();
    }

    public String account() {
        return arcCommonOptions.account();
    }

    public String region() {
        return arcCommonOptions.region();
    }

    public String stage() {
        return arcCommonOptions.stage();
    }

    public List<String> lots() {
        return rangeOrLot.lots;
    }

    public List<String> names() {
        return names;
    }

    public List<ArcState> states() {
        return states;
    }

    public List<String> sources() {
        return sources;
    }
}
