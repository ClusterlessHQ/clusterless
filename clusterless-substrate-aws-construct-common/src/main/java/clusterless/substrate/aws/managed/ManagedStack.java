/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.managed.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

/**
 *
 */
public class ManagedStack extends Stack implements Managed {
    private final ManagedProject project;
    private final Label baseId;

    public ManagedStack(@NotNull ManagedProject project, @NotNull Label baseId, @NotNull StackProps props) {
        super(project, baseId.camelCase(), props);
        this.project = project;
        this.baseId = baseId;
    }

    public ManagedStack(@NotNull ManagedProject project, Label baseId) {
        this(Names.stackName(project, baseId), project, baseId);
    }

    public ManagedStack(@NotNull Label stackName, @NotNull ManagedProject project, Label baseId) {
        super(project, baseId.camelCase(),
                StackProps.builder()
                        .env(environment(project))
                        .stackName(stackName.lowerHyphen())
                        .build()
        );

        this.project = project;
        this.baseId = baseId;
    }

    private static Environment environment(ManagedProject project) {
        return Environment.builder()
                .account(project.projectModel().target().account())
                .region(project.projectModel().target().region())
                .build();
    }

    public ManagedProject project() {
        return project;
    }

    @Override
    public Label baseId() {
        return baseId;
    }
}
