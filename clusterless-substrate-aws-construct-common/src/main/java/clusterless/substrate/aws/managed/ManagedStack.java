/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.model.Project;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

/**
 *
 */
public class ManagedStack extends Stack implements Managed {
    private final ManagedProject managedProject;
    private final Project project;
    private final Label baseId;

    private ManagedStack(@NotNull ManagedProject managedProject, @NotNull Project project, @NotNull Label baseId, @NotNull StackProps props) {
        super(managedProject, baseId.camelCase(), props);
        this.managedProject = managedProject;
        this.project = project;
        this.baseId = baseId;
    }

    public ManagedStack(@NotNull ManagedProject managedProject, @NotNull Project project, @NotNull Label baseId) {
        this(Names.stackName(project, baseId), managedProject, project, baseId);
    }

    public ManagedStack(@NotNull Label stackName, @NotNull ManagedProject managedProject, @NotNull Project project, @NotNull Label baseId) {
        super(managedProject, baseId.camelCase(),
                StackProps.builder()
                        .env(environment(project))
                        .stackName(stackName.lowerHyphen())
                        .build()
        );

        this.managedProject = managedProject;
        this.project = project;
        this.baseId = baseId;
    }

    private static Environment environment(Project project) {
        return Environment.builder()
                .account(project.target().account())
                .region(project.target().region())
                .build();
    }

    public ManagedProject managedProject() {
        return managedProject;
    }

    public Project project() {
        return project;
    }

    @Override
    public Label baseId() {
        return baseId;
    }
}
