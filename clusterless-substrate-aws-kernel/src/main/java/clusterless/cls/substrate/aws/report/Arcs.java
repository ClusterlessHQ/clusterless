/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.entity.ArcsCommandOptions;
import clusterless.cls.model.deploy.Project;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.ArcURI;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@CommandLine.Command(
        name = "arcs",
        description = "Operations on arcs",
        subcommands = {
                ArcStatus.class,
                ArcExec.class
        }
)
public class Arcs extends Reports implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(Arcs.class);

    @CommandLine.Mixin
    ArcsCommandOptions commandOptions = new ArcsCommandOptions();

    @Override
    public Integer call() throws Exception {
        Stream<ArcRecord> records = listAllArcs();

        Reporter<ArcRecord> reporter = Reporter.instance(kernel().printer(), ArcRecord.class);

        reporter.report(records);

        return 0;
    }

    @NotNull
    public Stream<ArcRecord> listAllArcs() {
        return listAllArcs(v -> true);
    }

    @NotNull
    public Stream<ArcRecord> listAllArcs(Predicate<ArcRecord> arcRecordPredicate) {
        S3 s3 = new S3(commandOptions.profile());

        Predicate<Project> sorted = projectFilter(commandOptions);

        return listAllProjects(commandOptions)
                .filter(r -> sorted.test(r.project))
                .map(r -> Map.entry(r.placement, listAllArcKeys(s3, r)))
                .flatMap(e -> e.getValue().stream().map(a -> Map.entry(e.getKey(), ArcURI.parse("/" + a))))
                .map(e -> new ArcRecord(e.getKey(), e.getValue().project(), e.getValue().arcName()))
                .filter(arcRecordPredicate)
                .peek(a -> LOG.info("found arc: {}", a.display()));
    }

    @NotNull
    private Predicate<Project> projectFilter(ArcsCommandOptions commandOptions) {
        if (commandOptions.projects().isEmpty()) {
            return p -> true;
        }

        Set<Project> collect = commandOptions.projects().stream()
                .map(Project::create)
                .collect(Collectors.toSet());

        Set<Project> sorted = new TreeSet<>(Project::compare);

        sorted.addAll(collect);

        return sorted::contains;
    }

    private static List<String> listAllArcKeys(S3 s3, ProjectRecord projectRecord) {
        URI uri = ArcURI.builder()
                .withPlacement(projectRecord.placement)
                .withProject(projectRecord.project)
                .build()
                .uriPrefix();

        S3.Response response = s3.listObjects(uri);

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to list projects in: %s, %s", uri, r.errorMessage())
        );

        return s3.listChildren(response);
    }
}
