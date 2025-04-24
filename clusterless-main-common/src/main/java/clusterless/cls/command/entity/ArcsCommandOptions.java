/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.entity;

import clusterless.cls.command.common.CommonCommandOptions;
import clusterless.cls.command.common.CommonOptions;
import picocli.CommandLine;

import java.util.List;

public class ArcsCommandOptions extends CommonCommandOptions {
    @CommandLine.Mixin
    ArcCommonOptions arcCommonOptions = new ArcCommonOptions();

    public void setArcCommonOptions(ArcCommonOptions arcCommonOptions) {
        this.arcCommonOptions = arcCommonOptions;
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
}
