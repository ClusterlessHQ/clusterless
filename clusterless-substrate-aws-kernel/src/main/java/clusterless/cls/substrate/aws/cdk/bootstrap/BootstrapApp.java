/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.bootstrap;

import clusterless.cls.substrate.aws.resources.ClsBootstrap;
import clusterless.commons.naming.Label;
import clusterless.commons.naming.Stage;
import clusterless.commons.naming.Version;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedApp;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.AppProps;

public class BootstrapApp extends ScopedApp {
    public BootstrapApp(@NotNull AppProps props, @NotNull Stage stage) {
        super(props, stage, Label.of(ClsBootstrap.BOOTSTRAP), Version.of(ClsBootstrap.BOOTSTRAP_VERSION), new BootstrapMeta(ClsBootstrap.BOOTSTRAP_VERSION));
    }
}
