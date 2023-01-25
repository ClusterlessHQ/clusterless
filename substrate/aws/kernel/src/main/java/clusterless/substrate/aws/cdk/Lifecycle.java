/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import clusterless.json.JSONUtil;
import clusterless.managed.component.ComponentContext;
import clusterless.managed.component.ComponentService;
import clusterless.managed.component.ComponentServices;
import clusterless.managed.component.ExtensibleType;
import clusterless.model.Project;
import clusterless.model.Resource;
import clusterless.substrate.aws.Manage;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedProject;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 *
 */
@CommandLine.Command(
        mixinStandardHelpOptions = true
)
public abstract class Lifecycle extends Manage implements Callable<Integer> {
    ComponentServices componentServices = ComponentServices.INSTANCE;

    @CommandLine.Option(names = {"-p", "--project"})
    String projectFile = "project.json";

    public Lifecycle() {

    }

    protected Project loadProjectModel() throws IOException {
        if (projectFile.equals("-")) {
            return JSONUtil.OBJECT_MAPPER.readValue(System.in, Project.class);
        }

        File file = Paths.get(projectFile).toFile();

        if (!file.exists()) {
            throw new FileNotFoundException("does not exist: " + projectFile);
        }

        return JSONUtil.OBJECT_MAPPER.readValue(file, Project.class);
    }

    protected void renderProject() throws IOException {
        Project projectModel = loadProjectModel();

        ManagedProject managedProject = mapProject(projectModel);

        managedProject.synth();
    }

    public ManagedProject mapProject(Project projectModel) {
        ManagedProject managed = new ManagedProject();

        ComponentContext context = new ManagedComponentContext(managed);

        for (Resource resource : projectModel.resources()) {
            String type = resource.type();
            Optional<ComponentService<ComponentContext, Resource>> service = componentServices.get(ExtensibleType.Resource, type);

            service.orElseThrow().create(context, resource);
        }

//        for (Boundary boundary : projectModel.ingressBoundaries()) {
//            Optional<ComponentService<ComponentContext>> service = componentServices.get(ComponentType.Boundary, "S3PutListenerBoundary");
//        }

        return managed;
    }
}
