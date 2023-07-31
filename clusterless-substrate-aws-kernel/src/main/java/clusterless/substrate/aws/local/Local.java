/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.local;

import clusterless.command.LocalCommandOptions;
import clusterless.managed.component.*;
import clusterless.model.DeployableLoader;
import clusterless.model.Model;
import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Deployable;
import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Workload;
import clusterless.substrate.aws.cdk.CDK;
import clusterless.util.Runtimes;
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
public class Local implements Callable<Integer> {
    @CommandLine.Mixin
    LocalCommandOptions commandOptions = new LocalCommandOptions();

    ComponentServices componentServices = ComponentServices.INSTANCE;

    public List<Deployable> loadProjectModels(List<File> deployFiles) throws IOException {
        return new DeployableLoader(deployFiles)
                .readObjects(CDK.PROVIDER);
    }

    @Override
    public Integer call() throws Exception {
        List<Deployable> deployables = loadProjectModels(commandOptions.projectFiles());

        Map<Deployable, List<Arc<?>>> found = new LinkedHashMap<>();

        for (Deployable deployable : deployables) {

            List<Arc<?>> arcs = deployable.arcs().stream()
                    .filter(a -> a.name().equalsIgnoreCase(commandOptions.arc()))
                    .collect(Collectors.toList());

            if (!arcs.isEmpty()) {
                found.put(deployable, arcs);
            }
        }

        List<Arc<? extends Workload<?>>> arcs = found
                .values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (arcs.isEmpty()) {
            System.err.println("no arcs found for: " + commandOptions.arc());
        }

        if (arcs.size() > 1) {
            System.err.println("too many arcs found for: " + commandOptions.arc() + ", found: " + arcs.stream().map(Arc::name).collect(Collectors.toList()));
        }

        Deployable deployable = found.keySet().stream().findFirst().orElseThrow();
        Arc<? extends Workload<?>> arc = found.get(deployable).get(0);

        ArcLocalExecutor executor = executorFor(deployable.placement(), arc);

        List<ArcLocalExecutor.Command> commands = executor.commands(commandOptions.role(), commandOptions.lotId(), commandOptions.manifestState());

        ShellWriter shellWriter = new ShellWriter(Runtimes.current());

        String script = shellWriter.toScript(commands);

        System.out.println(script);

        return 0;
    }

    private ArcLocalExecutor executorFor(Placement placement, Arc<? extends Workload<?>> arc) {
        Optional<ComponentService<ComponentContext, Model, Component>> componentService = componentServices.componentServicesForArc(arc);

        if (componentService.isEmpty()) {
            throw new IllegalStateException("unknown component type: " + arc.type());
        }

        return ((ArcComponentService<?, ?, ?>) componentService.get()).executor(placement, arc);
    }
}
