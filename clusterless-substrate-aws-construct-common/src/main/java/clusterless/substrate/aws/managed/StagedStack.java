/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

/**
 *
 */
public class StagedStack extends Stack {
    private final Label stage;

    public StagedStack(@NotNull StagedApp app, @Nullable String id, @Nullable StackProps props) {
        super(app, id, props);

        stage = app.stage();
    }

    public StagedStack(@NotNull StagedApp app, @Nullable String id) {
        super(app, id);

        stage = app.stage();
    }

    public StagedStack(@NotNull StagedApp app) {
        super(app);

        stage = app.stage();
    }

    public Label stage() {
        return stage;
    }
}
