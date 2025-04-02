/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.local;

import clusterless.cls.command.project.LocalCommandOptions;
import clusterless.cls.managed.component.*;
import clusterless.cls.managed.dataset.DatasetResolver;
import clusterless.cls.model.DeployableLoader;
import clusterless.cls.model.Model;
import clusterless.cls.model.deploy.*;
import clusterless.cls.substrate.aws.CommonCommand;
import clusterless.cls.substrate.aws.cdk.Provider;
import clusterless.cls.substrate.aws.util.Lookup;
import clusterless.commons.util.Runtimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 *
 */
@CommandLine.Command(
        name = "local"
)
public class Local extends CommonCommand implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(Local.class);

    @CommandLine.Mixin
    LocalCommandOptions commandOptions = new LocalCommandOptions();

    ComponentServices componentServices = ComponentServices.INSTANCE;

    public List<Deployable> loadProjectModels(List<File> deployFiles) throws IOException {
        return new DeployableLoader(deployFiles)
                .readObjects(Provider.NAME);
    }

    @Override
    public Integer call() throws Exception {
        List<Deployable> deployables = loadProjectModels(commandOptions.projectFiles());

        List<ExecCommand> commands = findArcs(deployables);

        if (commands.isEmpty()) {
            commands = findActivities(deployables);
        }

        if (commands.isEmpty()) {
            System.err.println("no arcs or activities found for: " + commandOptions.name());
            return 1;
        }

        ShellWriter shellWriter = new ShellWriter(Runtimes.current());

        String script = shellWriter.toScript(commands, commandOptions.dockerImage());

        System.out.println(script);

        return 0;
    }

    protected List<ExecCommand> findArcs(List<Deployable> deployables) {
        String profile = System.getenv().get("AWS_PROFILE");

        LOG.info("using AWS_PROFILE for lookup: {}", profile);

        DatasetResolver resolver = Lookup.createResolver(profile, deployables);

        Map<Deployable, List<Arc<?>>> found = new LinkedHashMap<>();

        for (Deployable deployable : deployables) {

            List<Arc<?>> arcs = deployable.arcs().stream()
                    .filter(a -> a.name().equalsIgnoreCase(commandOptions.name()))
                    .collect(Collectors.toList());

            if (!arcs.isEmpty()) {
                found.put(deployable, arcs);
            }
        }

        List<Arc<? extends Workload<?>>> arcs = found
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();

        if (arcs.isEmpty()) {
            return List.of();
        }

        if (arcs.size() > 1) {
            System.err.println("too many arcs found for: " + commandOptions.name() + ", found: " + arcs.stream()
                    .map(Arc::name)
                    .toList());
        }

        Deployable deployable = found.keySet().stream().findFirst().orElseThrow();
        Arc<? extends Workload<?>> arc = found.get(deployable).get(0);

        ArcLocalExecutor executor = executorFor(deployable.placement(), arc);

        String lotId = prompt(commandOptions.lotId(), "Enter lot id: ");

        boolean runInDocker = commandOptions.dockerImage() != null;

        return executor.commands(
                commandOptions.role(),
                lotId,
                commandOptions.manifestState(),
                source -> resolver.locate(deployable.placement(), deployable.project(), source),
                runInDocker
        );
    }

    protected List<ExecCommand> findActivities(List<Deployable> deployables) {
        Map<Deployable, List<Activity>> found = new LinkedHashMap<>();

        for (Deployable deployable : deployables) {

            List<Activity> activities = deployable.activities().stream()
                    .filter(a -> a.name().equalsIgnoreCase(commandOptions.name()))
                    .collect(Collectors.toList());

            if (!activities.isEmpty()) {
                found.put(deployable, activities);
            }
        }

        List<Activity> activities = found
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();

        if (activities.isEmpty()) {
            return List.of();
        }

        if (activities.size() > 1) {
            System.err.println("too many activities found for: " + commandOptions.name() + ", found: " + activities.stream()
                    .map(Activity::name)
                    .toList());
        }

        Deployable deployable = found.keySet().stream().findFirst().orElseThrow();

        Activity activity = found.get(deployable).get(0);

        ActivityLocalExecutor executor = executorFor(deployable.placement(), activity);

        boolean runInDocker = commandOptions.dockerImage() != null;

        return executor.commands(runInDocker);
    }

    private ArcLocalExecutor executorFor(Placement placement, Arc<? extends Workload<?>> arc) {
        Optional<ComponentService<ComponentContext, Model, Component>> componentService = componentServices.componentServicesForArc(arc);

        if (componentService.isEmpty()) {
            throw new IllegalStateException("unknown component type: " + arc.type());
        }

        return ((ArcComponentService<?, ?, ?>) componentService.get()).executor(placement, arc);
    }

    private ActivityLocalExecutor executorFor(Placement placement, Activity activity) {
        Optional<ComponentService<ComponentContext, Model, Component>> componentService = componentServices.componentServicesForActivity(activity);

        if (componentService.isEmpty()) {
            throw new IllegalStateException("unknown component type: " + activity.type());
        }

        return ((ActivityComponentService<?, ?, ?>) componentService.get()).executor(placement, activity);
    }
}
