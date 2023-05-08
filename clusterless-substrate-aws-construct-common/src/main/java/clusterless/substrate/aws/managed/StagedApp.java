/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.naming.Stage;
import clusterless.substrate.aws.resources.DeployMeta;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.constructs.Construct;

/**
 *
 */
public class StagedApp extends App {
    public static StagedApp stagedOf(Construct scope) {
        return (StagedApp) scope.getNode().getRoot();
    }

    Stage stage;

    DeployMeta deployMeta = new DeployMeta();

    public StagedApp(@NotNull AppProps props, @NotNull Stage stage) {
        super(props);
        this.stage = stage;
    }

    public Stage stage() {
        return stage;
    }

    public StagedApp setDeployMeta(DeployMeta deployMeta) {
        this.deployMeta = deployMeta;
        return this;
    }

    public DeployMeta deployMeta() {
        return deployMeta;
    }
}
