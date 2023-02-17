/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import clusterless.managed.component.*;
import clusterless.model.Model;
import clusterless.model.deploy.Deployable;
import clusterless.model.deploy.Extensible;
import clusterless.startup.Loader;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.ManagedStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
        List<Deployable> deployableModels = loadProjectModels(projectFiles);

        ManagedProject managedProject = mapProject(deployableModels);

        managedProject.synth();
    }

    protected List<Deployable> loadProjectModels(List<File> deployFiles) throws IOException {
        return new Loader(deployFiles)
                .readObjects(CDK.PROVIDER, Deployable.PROVIDER_POINTER, Deployable.class, Deployable::setSourceFile);
    }

    public ManagedProject mapProject(List<Deployable> deployables) {
        Set<String> names = verifyNonNull("name", deployables.stream().map(d -> d.project().name()).collect(Collectors.toSet()));
        Set<String> versions = verifyNonNull("version", deployables.stream().map(d -> d.project().version()).collect(Collectors.toSet()));
        Set<String> stages = verify("stage", deployables.stream().map(d -> d.placement().stage()).collect(Collectors.toSet()));

        String name = names.stream().findFirst().orElseThrow();
        String version = versions.stream().findFirst().orElseThrow();
        String stage = stages.isEmpty() ? null : stages.stream().findFirst().orElseThrow();

        ManagedProject managedProject = new ManagedProject(name, version, stage, deployables);

        for (Deployable deployable : deployables) {
            verifyComponentsAreAvailable(deployable);

            // deploy provided stacks
            Map<Extensible, ComponentService<ComponentContext, Model>> containers = getMangedTypesFor(ManagedType.container, ModelType.values(), deployable, new LinkedHashMap<>());

            if (!containers.isEmpty()) {
                construct(new ManagedComponentContext(managedProject, deployable), containers);
            }

            // create a stack for resource member constructs
            construct(managedProject, deployable, ModelType.Resource);

            // create a stack for boundary member constructs
            construct(managedProject, deployable, ModelType.Boundary);

            // create arcs with Process member constructs
            // unsupported
        }

        return managedProject;
    }

    private static Set<String> verifyNonNull(String propertyName, Set<String> values) {
        if (values.contains(null)) {
            throw new IllegalStateException("all project files must have non-null " + propertyName);
        }

        return verify(propertyName, values);
    }

    private static Set<String> verify(String propertyName, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalStateException("all project files must have the same " + propertyName + ", got: " + values);
        }

        values.remove(null);

        return values;
    }

    private void construct(ManagedProject managedProject, Deployable deployable, ModelType modelType) {
        Map<Extensible, ComponentService<ComponentContext, Model>> memberResources = getMangedTypesFor(ManagedType.member, ModelType.values(modelType), deployable, new LinkedHashMap<>());

        if (memberResources.isEmpty()) {
            return;
        }

        ManagedStack stack = new ManagedStack(managedProject, deployable, modelType);

        managedProject.stacks().forEach(stack::addDependency);

        ComponentContext context = new ManagedComponentContext(managedProject, deployable, stack);

        construct(context, memberResources);

    }

    private static void construct(ComponentContext containerContext, Map<Extensible, ComponentService<ComponentContext, Model>> containers) {
        containers.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> {
            Extensible key = e.getKey();
            LOG.info("creating %s construct: %s".formatted(key.getClass().getSimpleName(), key.type()));
            e.getValue().create(containerContext, key);
        });
    }

    private void verifyComponentsAreAvailable(Deployable deployableModel) {
        // accumulate all providers for all declared model types
        Map<Extensible, ComponentService<ComponentContext, Model>> map = new LinkedHashMap<>();

        for (ManagedType managedType : ManagedType.values()) {
            getMangedTypesFor(managedType, ModelType.values(), deployableModel, map);
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

    private Map<Extensible, ComponentService<ComponentContext, Model>> getMangedTypesFor(ManagedType managedType, ModelType[] modelTypes, Deployable deployableModel, Map<Extensible, ComponentService<ComponentContext, Model>> map) {
        EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model>>> containerMap = componentServices.componentServicesFor(managedType);

        for (ModelType modelType : modelTypes) {
            if (!containerMap.containsKey(modelType)) {
                continue;
            }

            for (Extensible extensible : getExtensiblesFor(modelType, deployableModel)) {
                // put a null if not available
                map.put(extensible, containerMap.get(modelType).get(extensible.type()));
            }
        }

        return map;
    }

    @NotNull
    private static List<Extensible> getExtensiblesFor(ModelType modelType, Deployable deployableModel) {
        List<Extensible> extensibles = Collections.emptyList();
        switch (modelType) {
            case Resource -> extensibles = new ArrayList<>(deployableModel.resources());
            case Boundary -> extensibles = new ArrayList<>(deployableModel.boundaries());
            case Process -> throw new UnsupportedOperationException();
        }
        return extensibles;
    }
}
