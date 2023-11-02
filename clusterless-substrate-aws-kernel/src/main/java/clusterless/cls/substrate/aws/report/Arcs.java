/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.ArcsCommandOptions;
import clusterless.cls.model.deploy.Project;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.ArcURI;
import org.jetbrains.annotations.NotNull;
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
        name = "arcs"
)
public class Arcs extends Reports implements Callable<Integer> {

    @CommandLine.Mixin
    ArcsCommandOptions commandOptions = new ArcsCommandOptions();

    private static int compare(Project o1, Project o2) {
        int i = o1.name().compareTo(o2.name());

        if (i == 0) {
            return 0;
        }

        if (o1.version() == null || o2.version() == null) {
            return i;
        }

        return o1.version().compareTo(o2.version());
    }

    @Override
    public Integer call() throws Exception {
        S3 s3 = new S3(commandOptions.profile());

        Predicate<Project> sorted = projectFilter();

        Stream<ArcRecord> records = listAllProjects(commandOptions)
                .filter(r -> sorted.test(r.project))
                .map(r -> Map.entry(r.placement, listAllArcKeys(s3, r)))
                .flatMap(e -> e.getValue().stream().map(a -> Map.entry(e.getKey(), ArcURI.parse("/" + a))))
                .map(e -> new ArcRecord(e.getKey(), e.getValue().project(), e.getValue().arcName()));

        Reporter<ArcRecord> reporter = Reporter.instance(kernel().printer(), ArcRecord.class);

        reporter.report(records);

        return 0;
    }

    @NotNull
    private Predicate<Project> projectFilter() {
        if (commandOptions.projects().isEmpty()) {
            return p -> true;
        }

        Set<Project> collect = commandOptions.projects().stream()
                .map(Project::create)
                .collect(Collectors.toSet());

        Set<Project> sorted = new TreeSet<>(Arcs::compare);

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
