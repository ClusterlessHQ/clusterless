/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.managed.Label;

/**
 *
 */
public class Names {
    public static Label stackName(ManagedProject project, Label baseId) {
        String stage = project.projectModel().target().stage();
        Label name = project.name();
        String version = project.version();
        String region = project.projectModel().target().region();

        return Label.of(stage).upperOnly()
                .with(Label.of(name))
                .with(baseId)
                .with(Label.of(version))
                .with(Label.of(region));
    }
}
