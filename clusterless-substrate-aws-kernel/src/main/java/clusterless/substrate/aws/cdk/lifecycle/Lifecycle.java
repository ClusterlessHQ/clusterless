/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk.lifecycle;

import clusterless.config.Configurations;
import clusterless.managed.component.*;
import clusterless.model.Model;
import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Deployable;
import clusterless.model.deploy.Extensible;
import clusterless.model.deploy.Workload;
import clusterless.startup.Loader;
import clusterless.substrate.aws.arc.ArcStack;
import clusterless.substrate.aws.cdk.CDK;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.ManagedStack;
import clusterless.util.Label;
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

    StackGroups stackGroups = new StackGroups();
    private Configurations configurations;

    public Lifecycle() {
    }

    public void setConfigurations(Configurations configurations) {
        this.configurations = configurations;
    }

    public void synthProject(List<File> projectFiles) throws IOException {
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

            // create a stack for each isolatable construct
            for (ModelType[] stackGroup : stackGroups.includableStackGroups()) {
                constructIsolatedStacks(managedProject, deployable, stackGroup);
            }

            // create a stack for includable constructs
            for (ModelType[] stackGroup : stackGroups.isolatableStackGroups()) {
                constructIncludedStack(managedProject, deployable, stackGroup);
            }

            // create a stack for includable constructs
            for (ModelType[] stackGroup : stackGroups.embeddedStackGroups()) {
                constructEmbeddedStacks(managedProject, deployable, stackGroup);
            }
        }

        return managedProject;
    }

    private void constructEmbeddedStacks(ManagedProject managedProject, Deployable deployable, ModelType[] embeddable) {
        Map<Extensible, ComponentService<ComponentContext, Model, Component>> embedded = getMangedTypesFor(Isolation.embedded, embeddable, deployable, new LinkedHashMap<>());

        if (embedded.isEmpty()) {
            LOG.info("found no embeddable models");
            return;
        }

        List<ManagedStack> priorStacks = new ArrayList<>(managedProject.stacks());

        for (Arc arc : deployable.arcs()) {
            Workload workload = arc.workload();

            if (workload == null) {
                throw new IllegalStateException("workload may not be null");
            }

            ComponentService<ComponentContext, Model, Component> modelComponentService = embedded.get(workload);

            if (modelComponentService == null) {
                String message = "component service not found in arc: %s, workload type: %s".formatted(arc.name(), workload.type());
                LOG.error(message);
                throw new IllegalStateException(message);
            }

            // construct a stack for every embeddable type, currently only have Process that embeds in an Arc
            ArcStack stack = new ArcStack(configurations, managedProject, deployable, arc);

            // force dependency on prior stacks, but not prior arcs
            priorStacks.forEach(stack::addDependency);

            ManagedComponentContext context = new ManagedComponentContext(configurations, managedProject, deployable, stack);
            WorkloadComponent construct = createConstructWith(context, modelComponentService, workload);

            stack.applyWorkloadComponent(construct);
        }
    }

    private void constructIsolatedStacks(ManagedProject managedProject, Deployable deployable, ModelType[] isolatable) {
        Map<Extensible, ComponentService<ComponentContext, Model, Component>> isolated = getMangedTypesFor(Isolation.isolated, isolatable, deployable, new LinkedHashMap<>());

        if (isolated.isEmpty()) {
            LOG.info("found no isolatable models");
            return;
        }

        // constructs a stack for every isolated declared model
        construct(new ManagedComponentContext(configurations, managedProject, deployable), isolated);
    }

    private void constructIncludedStack(ManagedProject managedProject, Deployable deployable, ModelType[] includable) {
        Map<Extensible, ComponentService<ComponentContext, Model, Component>> included = getMangedTypesFor(Isolation.included, includable, deployable, new LinkedHashMap<>());

        if (included.isEmpty()) {
            LOG.info("found no includable models");
            return;
        }

        // constructs one stack for all included models types in this grouping
        ManagedStack stack = new ManagedStack(managedProject, deployable, Label.concat(includable));

        // make the new stack dependent on the prior stacks so order is retained during deployment
        managedProject.stacks().forEach(stack::addDependency);

        ComponentContext context = new ManagedComponentContext(configurations, managedProject, deployable, stack);

        construct(context, included);
    }

    private static void construct(ComponentContext context, Map<Extensible, ComponentService<ComponentContext, Model, Component>> containers) {
        containers.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> {
            Extensible extensible = e.getKey();
            ComponentService<ComponentContext, Model, Component> modelComponentService = e.getValue();
            createConstructWith(context, modelComponentService, extensible);
        });
    }

    private static <C extends Component> C createConstructWith(ComponentContext context, ComponentService<ComponentContext, Model, Component> modelComponentService, Extensible extensible) {
        LOG.info("creating %s construct: %s".formatted(extensible.getClass().getSimpleName(), extensible.type()));
        return (C) modelComponentService.create(context, extensible);
    }

    private void verifyComponentsAreAvailable(Deployable deployableModel) {
        // accumulate all providers for all declared model types
        Map<Extensible, ComponentService<ComponentContext, Model, Component>> map = new LinkedHashMap<>();

        for (Isolation isolation : Isolation.values()) {
            getMangedTypesFor(isolation, ModelType.values(), deployableModel, map);
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

    private Map<Extensible, ComponentService<ComponentContext, Model, Component>> getMangedTypesFor(Isolation isolation, ModelType[] modelTypes, Deployable deployableModel, Map<Extensible, ComponentService<ComponentContext, Model, Component>> map) {
        EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model, Component>>> containerMap = componentServices.componentServicesFor(isolation);

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
            case Workload ->
                    extensibles = deployableModel.arcs().stream().map(Arc::workload).collect(Collectors.toList());
        }
        return extensibles;
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

}
