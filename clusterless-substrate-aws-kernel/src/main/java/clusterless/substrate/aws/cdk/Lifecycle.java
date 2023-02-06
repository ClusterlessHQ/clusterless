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
import clusterless.model.Deploy;
import clusterless.model.Extensible;
import clusterless.model.Model;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.ManagedStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class Lifecycle {
    private static final Logger LOG = LogManager.getLogger(Lifecycle.class);

    ComponentServices componentServices = ComponentServices.INSTANCE;

    public Lifecycle() {

    }

    protected void synthProject(List<File> projectFiles) throws IOException {
        List<Deploy> deployModels = loadProjectModels(projectFiles);

        ManagedProject managedProject = mapProject(deployModels);

        managedProject.synth();
    }

    protected List<Deploy> loadProjectModels(List<File> projectFiles) throws IOException {
        if (projectFiles.isEmpty()) {
            throw new IllegalStateException("no project files declared");
        }

        if (projectFiles.get(0).toString().equals("-")) {
            return List.of(JSONUtil.OBJECT_MAPPER.readValue(System.in, Deploy.class));
        }

        List<Deploy> results = new LinkedList<>();

        for (File projectFile : projectFiles) {
            if (!projectFile.exists()) {
                throw new FileNotFoundException("does not exist: " + projectFile);
            }

            Deploy deploy = JSONUtil.OBJECT_MAPPER.readValue(projectFile, Deploy.class);

            deploy.setSourceFile(projectFile);

            results.add(deploy);
        }

        return results;
    }

    public ManagedProject mapProject(List<Deploy> deploys) {
        Set<String> names = deploys.stream().map(d -> d.project().name()).collect(Collectors.toSet());
        Set<String> versions = deploys.stream().map(d -> d.project().version()).collect(Collectors.toSet());

        if (names.size() > 1) {
            throw new IllegalStateException("all project files must have the same name, got: " + names);
        }

        if (versions.size() > 1) {
            throw new IllegalStateException("all project files must have the same version, got: " + versions);
        }

        String name = names.stream().findFirst().orElseThrow();
        String version = versions.stream().findFirst().orElseThrow();

        ManagedProject managedProject = new ManagedProject(name, version, deploys);

        for (Deploy deploy : deploys) {
            verifyComponentsAreAvailable(deploy);

            // deploy provided stacks
            Map<Extensible, ComponentService<ComponentContext, Model>> containers = getMangedTypesFor(ManagedType.container, ModelType.values(), deploy, new LinkedHashMap<>());

            if (!containers.isEmpty()) {
                construct(new ManagedComponentContext(managedProject, deploy), containers);
            }

            // create a stack for resource member constructs
            construct(managedProject, deploy, ModelType.Resource);

            // create a stack for boundary member constructs
            construct(managedProject, deploy, ModelType.Boundary);

            // create arcs with Process member constructs
            // unsupported
        }

        return managedProject;
    }

    private void construct(ManagedProject managedProject, Deploy deploy, ModelType modelType) {
        Map<Extensible, ComponentService<ComponentContext, Model>> memberResources = getMangedTypesFor(ManagedType.member, ModelType.values(modelType), deploy, new LinkedHashMap<>());

        if (memberResources.isEmpty()) {
            return;
        }

        ManagedStack resourceStack = new ManagedStack(managedProject, deploy, modelType);
        ComponentContext resourceContext = new ManagedComponentContext(managedProject, deploy, resourceStack);

        construct(resourceContext, memberResources);
    }

    private static void construct(ComponentContext containerContext, Map<Extensible, ComponentService<ComponentContext, Model>> containers) {
        containers.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> {
            Extensible key = e.getKey();
            LOG.info("creating %s construct: %s".formatted(key.getClass().getSimpleName(), key.type()));
            e.getValue().create(containerContext, key);
        });
    }

    private void verifyComponentsAreAvailable(Deploy deployModel) {
        // accumulate all providers for all declared model types
        Map<Extensible, ComponentService<ComponentContext, Model>> map = new LinkedHashMap<>();

        for (ManagedType managedType : ManagedType.values()) {
            getMangedTypesFor(managedType, ModelType.values(), deployModel, map);
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

    private Map<Extensible, ComponentService<ComponentContext, Model>> getMangedTypesFor(ManagedType managedType, ModelType[] modelTypes, Deploy deployModel, Map<Extensible, ComponentService<ComponentContext, Model>> map) {
        EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model>>> containerMap = componentServices.componentServicesFor(managedType);

        for (ModelType modelType : modelTypes) {
            if (!containerMap.containsKey(modelType)) {
                continue;
            }

            for (Extensible extensible : getExtensiblesFor(modelType, deployModel)) {
                // put a null if not available
                map.put(extensible, containerMap.get(modelType).get(extensible.type()));
            }
        }

        return map;
    }

    @NotNull
    private static List<Extensible> getExtensiblesFor(ModelType modelType, Deploy deployModel) {
        List<Extensible> extensibles = Collections.emptyList();
        switch (modelType) {
            case Resource -> extensibles = new ArrayList<>(deployModel.resources());
            case Boundary -> extensibles = new ArrayList<>(deployModel.boundaries());
            case Process -> throw new UnsupportedOperationException();
        }
        return extensibles;
    }
}
