/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.lifecycle;

import clusterless.cls.config.Configurations;
import clusterless.cls.json.JSONUtil;
import clusterless.cls.managed.ModelType;
import clusterless.cls.managed.component.*;
import clusterless.cls.managed.dataset.DatasetResolver;
import clusterless.cls.model.DeployableLoader;
import clusterless.cls.model.Model;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.model.deploy.Extensible;
import clusterless.cls.model.deploy.Workload;
import clusterless.cls.substrate.aws.arc.ArcStack;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import clusterless.cls.substrate.aws.cdk.Provider;
import clusterless.cls.substrate.aws.construct.ArcConstruct;
import clusterless.cls.substrate.aws.construct.EgressBoundaryConstruct;
import clusterless.cls.substrate.aws.construct.IngressBoundaryConstruct;
import clusterless.cls.substrate.aws.construct.ResourceConstruct;
import clusterless.cls.substrate.aws.managed.ManagedApp;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.managed.ManagedStack;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.DatasetURI;
import clusterless.commons.naming.Label;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.constructs.Construct;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class Lifecycle {
    private static final Logger LOG = LogManager.getLogger(Lifecycle.class);

    ComponentServices componentServices = ComponentServices.INSTANCE;

    StackGroups stackGroups = new StackGroups();
    Configurations configurations;

    public Lifecycle() {
    }

    public void setConfigurations(Configurations configurations) {
        this.configurations = configurations;
    }

    public void synthProject(List<File> projectFiles) throws IOException {
        synthProjectModels(loadProjectModels(projectFiles));
    }

    public void synthProjectModels(List<Deployable> deployableModels) {
        ManagedApp managedApp = mapProject(deployableModels);

        managedApp.synth();
    }

    public List<Deployable> loadProjectModels(List<File> deployFiles) throws IOException {
        return new DeployableLoader(deployFiles)
                .readObjects(Provider.NAME);
    }

    public ManagedApp mapProject(List<Deployable> deployables) {
        Set<String> names = verifyNonNull("name", deployables.stream().map(d -> d.project().name()).collect(Collectors.toSet()));
        Set<String> versions = verifyNonNull("version", deployables.stream().map(d -> d.project().version()).collect(Collectors.toSet()));
        Set<String> stages = verify("stage", deployables.stream().map(d -> d.placement().stage()).collect(Collectors.toSet()));

        String name = names.stream().findFirst().orElseThrow();
        String version = versions.stream().findFirst().orElseThrow();
        String stage = stages.isEmpty() ? null : stages.stream().findFirst().orElseThrow();

        String profile = System.getenv().get(CDKProcessExec.CLS_CDK_PROFILE);

        DatasetResolver resolver = createResolver(profile, deployables);

        ManagedApp managedApp = new ManagedApp(name, version, stage, deployables);

        for (Deployable deployable : deployables) {
            verifyComponentsAreAvailable(deployable);

            // create a stack for each isolatable construct
            for (ModelType[] stackGroup : stackGroups.independentModels()) {
                constructIndependentStacks(resolver, managedApp, deployable, stackGroup);
            }

            // create a stack for grouped constructs
            for (ModelType[] stackGroup : stackGroups.groupedModels()) {
                constructGroupedStack(resolver, managedApp, deployable, stackGroup);
            }

            // create a stack for independent constructs
            for (ModelType[] stackGroup : stackGroups.managedModels()) {
                constructManagedStacks(resolver, managedApp, deployable, stackGroup);
            }
        }

        return managedApp;
    }

    @NotNull
    private static DatasetResolver createResolver(String profile, List<Deployable> deployables) {
        return new DatasetResolver(
                deployables,
                (placement, source) -> {
                    S3 s3 = new S3(profile, placement.region());

                    URI datasetURI = DatasetURI.builder()
                            .withPlacement(placement)
                            .withDataset(source)
                            .build()
                            .uri();

                    S3.Response response = s3.get(datasetURI);

                    if (!s3.exists(response)) {
                        return Optional.empty();
                    }

                    return Optional.of(JSONUtil.readAsObjectSafe(response.asInputStream(), new TypeReference<>() {}));
                }
        );
    }

    private void constructManagedStacks(DatasetResolver resolver, ManagedApp managedApp, Deployable deployable, ModelType[] independent) {
        Map<Extensible, ComponentService<ComponentContext, Model, Component>> managed = componentServices.componentServicesFor(Isolation.managed, deployable, independent);

        if (managed.isEmpty()) {
            LOG.info("found no managed models");
            return;
        }

        List<ManagedStack> priorStacks = new ArrayList<>(managedApp.stacks());

        for (Arc<? extends Workload<?>> arc : deployable.arcs()) {
            ComponentService<ComponentContext, Model, Component> modelComponentService = managed.get(arc);

            if (modelComponentService == null) {
                String message = String.format("component service not found in arc: %s, type: %s", arc.name(), arc.type());
                LOG.error(message);
                throw new IllegalStateException(message);
            }

            if (arc.exclude()) {
                LOG.info("excluding arc type: {}", arc.type());
                continue;
            }

            // construct a stack for every arc
            ArcStack stack = new ArcStack(configurations, resolver, managedApp, deployable, arc);

            // force dependency on prior stacks, but not prior arcs
            priorStacks.forEach(stack::addDependency);

            // todo: lookup all referenced datasets and retrieve their bucket names
            ManagedComponentContext context = new ManagedComponentContext(configurations, resolver, managedApp, deployable, stack);
            LOG.info(String.format("creating %s embedded construct: %s", arc.label().camelCase(), arc.type()));
            ArcComponent construct = (ArcComponent) modelComponentService.create(context, arc);

            stack.applyArcWorkloadComponent(construct);
        }

    }

    private void constructIndependentStacks(DatasetResolver resolver, ManagedApp managedApp, Deployable deployable, ModelType[] isolatable) {
        Map<Extensible, ComponentService<ComponentContext, Model, Component>> isolated = componentServices.componentServicesFor(Isolation.independent, deployable, isolatable);

        if (isolated.isEmpty()) {
            LOG.info("found no independent models");
            return;
        }

        // constructs a stack for every isolated declared model
        construct(new ManagedComponentContext(configurations, resolver, managedApp, deployable), isolated);
    }

    private void constructGroupedStack(DatasetResolver resolver, ManagedApp managedApp, Deployable deployable, ModelType[] includable) {
        Map<Extensible, ComponentService<ComponentContext, Model, Component>> included = componentServices.componentServicesFor(Isolation.grouped, deployable, includable);

        if (included.isEmpty()) {
            LOG.info("found no grouped models");
            return;
        }

        // constructs one stack for all included models types in this grouping
        ManagedStack stack = new ManagedStack(managedApp, deployable, Label.concat(includable));

        // make the new stack dependent on the prior stacks so order is retained during deployment
        managedApp.stacks().forEach(stack::addDependency);

        ComponentContext context = new ManagedComponentContext(configurations, resolver, managedApp, deployable, stack);

        construct(context, included);
    }

    private static void construct(ComponentContext context, Map<Extensible, ComponentService<ComponentContext, Model, Component>> containers) {
        Multimap<Class<? extends Construct>, Construct> map = LinkedListMultimap.create();

        containers.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> {
            Extensible extensible = e.getKey();

            if (extensible.exclude()) {
                LOG.info("excluding {} type: {}", extensible.label().camelCase(), extensible.type());
                return;
            }

            ComponentService<ComponentContext, Model, Component> modelComponentService = e.getValue();
            LOG.info("creating {} construct: {}", extensible.label().camelCase(), extensible.type());

            Component component = modelComponentService.create(context, extensible);

            lookupModel(component)
                    .ifPresent(type -> map.put(type, (Construct) component));
        });

        for (Construct resource : map.get(ResourceConstruct.class)) {
            map.get(ArcConstruct.class).forEach(c -> c.getNode().addDependency(resource));
            map.get(EgressBoundaryConstruct.class).forEach(c -> c.getNode().addDependency(resource));
            map.get(IngressBoundaryConstruct.class).forEach(c -> c.getNode().addDependency(resource));
        }
    }

    private static Optional<Class<? extends Construct>> lookupModel(Component component) {
        if (component instanceof ArcConstruct<?>) {
            return Optional.of(ArcConstruct.class);
        }
        if (component instanceof ResourceConstruct<?>) {
            return Optional.of(ResourceConstruct.class);
        }
        if (component instanceof EgressBoundaryConstruct<?>) {
            return Optional.of(EgressBoundaryConstruct.class);
        }
        if (component instanceof IngressBoundaryConstruct<?>) {
            return Optional.of(IngressBoundaryConstruct.class);
        }
        return Optional.empty();
    }

    private void verifyComponentsAreAvailable(Deployable deployableModel) {
        // accumulate all providers for all declared model types
        Map<Extensible, ComponentService<ComponentContext, Model, Component>> map = new LinkedHashMap<>();

        for (Isolation isolation : Isolation.values()) {
            map.putAll(componentServices.componentServicesFor(isolation, deployableModel, ModelType.values()));
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
