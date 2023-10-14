/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.ProjectsCommandOptions;
import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.ProjectURI;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import one.util.streamex.EntryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 *
 */
@CommandLine.Command(
        name = "projects"
)
public class Projects extends Reports implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(Projects.class);

    static class Record implements Struct {
        @JsonUnwrapped
        Placement placement;
        @JsonUnwrapped
        Project project;

        public Record(Placement placement, Project project) {
            this.placement = placement;
            this.project = project;
        }
    }

    @CommandLine.Mixin
    ProjectsCommandOptions commandOptions = new ProjectsCommandOptions();

    @Override
    public Integer call() throws Exception {
        LOG.info("using profile: {}", commandOptions.profile());

        S3 s3 = new S3(commandOptions.profile());

        List<Placement> placements = filterPlacements(commandOptions);

        Map<Placement, List<String>> results = new LinkedHashMap<>();

        for (Placement placement : placements) {
            URI uri = ProjectURI.builder()
                    .withPlacement(placement)
                    .build().uriPrefix();

            S3.Response response = s3.listObjects(uri);

            response.isSuccessOrThrowRuntime(
                    r -> String.format("unable to list projects in: %s, %s", uri, r.errorMessage())
            );

            results.put(
                    placement,
                    s3.listChildren(response)
            );
        }

        Stream<Map.Entry<Placement, List<String>>> stream = results.entrySet().stream();
        Stream<Record> records = EntryStream.of(stream)
                .flatMapKeyValue((k, v) -> v.stream()
                        .map("/"::concat)
                        .map(ProjectURI::parse)
                        .map(ProjectURI::project)
                        .map(p -> new Record(k, p))
                );

        Reporter<Record> reporter = Reporter.instance(kernel().printer(), Record.class);

        reporter.report(records);

        return 0;
    }
}
