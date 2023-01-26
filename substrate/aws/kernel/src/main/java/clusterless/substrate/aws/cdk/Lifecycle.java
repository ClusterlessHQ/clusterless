/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import clusterless.json.JSONUtil;
import clusterless.managed.component.*;
import clusterless.model.Extensible;
import clusterless.model.Model;
import clusterless.model.Project;
import clusterless.substrate.aws.Manage;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.ManagedStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 *
 */
@CommandLine.Command(
        mixinStandardHelpOptions = true
)
public abstract class Lifecycle extends Manage implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Lifecycle.class);

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
        ManagedProject managedProject = new ManagedProject(projectModel);

        verifyComponentsAreAvailable(projectModel);

        // deploy provided stacks
        Map<Extensible, ComponentService<ComponentContext, Model>> containers = getMangedTypesFor(ManagedType.container, ModelType.values(), projectModel, new LinkedHashMap<>());

        if (!containers.isEmpty()) {
            construct(new ManagedComponentContext(managedProject), containers);
        }

        // create a stack for resource member constructs
        construct(projectModel, managedProject, ModelType.Resource);

        // create a stack for boundary member constructs
        construct(projectModel, managedProject, ModelType.Boundary);

        // create arcs with Process member constructs
        // unsupported

        return managedProject;
    }

    private void construct(Project projectModel, ManagedProject managedProject, ModelType modelType) {
        Map<Extensible, ComponentService<ComponentContext, Model>> memberResources = getMangedTypesFor(ManagedType.member, ModelType.values(modelType), projectModel, new LinkedHashMap<>());

        if (memberResources.isEmpty()) {
            return;
        }

        ManagedStack resourceStack = new ManagedStack(managedProject, modelType);
        ComponentContext resourceContext = new ManagedComponentContext(managedProject, resourceStack);

        construct(resourceContext, memberResources);
    }

    private static void construct(ComponentContext containerContext, Map<Extensible, ComponentService<ComponentContext, Model>> containers) {
        containers.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> {
            Extensible key = e.getKey();
            LOG.info("creating %s construct: %s".formatted(key.getClass().getSimpleName(), key.type()));
            e.getValue().create(containerContext, key);
        });
    }

    private void verifyComponentsAreAvailable(Project projectModel) {
        // accumulate all providers for all declared model types
        Map<Extensible, ComponentService<ComponentContext, Model>> map = new LinkedHashMap<>();

        for (ManagedType managedType : ManagedType.values()) {
            getMangedTypesFor(managedType, ModelType.values(), projectModel, map);
        }

        Set<String> missing = map.entrySet()
                .stream()
                .filter(e -> e.getValue() == null)
                .map(e -> e.getKey().type())
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            missing.forEach(type -> LOG.error("unable to find component provider for: " + type));
            throw new IllegalStateException("found missing providers: " + missing);
        }
    }

    private Map<Extensible, ComponentService<ComponentContext, Model>> getMangedTypesFor(ManagedType managedType, ModelType[] modelTypes, Project projectModel, Map<Extensible, ComponentService<ComponentContext, Model>> map) {
        EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model>>> containerMap = componentServices.componentServicesFor(managedType);

        for (ModelType modelType : modelTypes) {
            if (!containerMap.containsKey(modelType)) {
                continue;
            }

            for (Extensible extensible : getExtensiblesFor(modelType, projectModel)) {
                // put a null if not available
                map.put(extensible, containerMap.get(modelType).get(extensible.type()));
            }
        }

        return map;
    }

    @NotNull
    private static List<Extensible> getExtensiblesFor(ModelType modelType, Project projectModel) {
        List<Extensible> extensibles = Collections.emptyList();
        switch (modelType) {
            case Resource -> extensibles = new ArrayList<>(projectModel.resources());
            case Boundary -> extensibles = new ArrayList<>(projectModel.boundaries());
            case Process -> throw new UnsupportedOperationException();
        }
        return extensibles;
    }
}
