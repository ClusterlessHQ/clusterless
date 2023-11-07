/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import picocli.CommandLine;

public class ReportOptions {
    @CommandLine.Option(
            names = "--profile",
            description = "Cloud profile."
    )
    String profile = System.getenv("AWS_PROFILE"); // todo: should be deferred to the substrate
    @CommandLine.Option(
            names = {"--account"},
            description = "Filter results by account."
    )
    String account;
    @CommandLine.Option(
            names = {"--region"},
            description = "Filter results by region."
    )
    String region;
    @CommandLine.Option(
            names = {"--stage"},
            description = "Filter results by stage.",
            fallbackValue = "",
            arity = "0..1"
    )
    String stage;

    public ReportOptions setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public ReportOptions setAccount(String account) {
        this.account = account;
        return this;
    }

    public ReportOptions setRegion(String region) {
        this.region = region;
        return this;
    }

    public ReportOptions setStage(String stage) {
        this.stage = stage;
        return this;
    }

    public String profile() {
        return profile;
    }

    public String account() {
        return account;
    }

    public String region() {
        return region;
    }

    public String stage() {
        return stage;
    }
}
