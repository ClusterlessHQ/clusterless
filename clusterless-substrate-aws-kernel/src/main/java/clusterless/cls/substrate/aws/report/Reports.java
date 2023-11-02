/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.ReportCommandOptions;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.substrate.aws.CommonCommand;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.store.StateStore;
import clusterless.cls.substrate.store.Stores;
import clusterless.cls.substrate.uri.ProjectURI;
import clusterless.commons.util.Strings;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Reports extends CommonCommand {

    @NotNull
    protected Stream<ProjectRecord> listAllProjects(ReportCommandOptions commandOptions) {
        List<Placement> placements = filterPlacements(commandOptions);

        Stream<ProjectRecord> records = StreamEx.empty();

        for (Placement placement : placements) {
            List<String> children = listAllProjectKeysFor(commandOptions.profile(), placement);

            Stream<ProjectRecord> recordStream = children.stream().map("/"::concat)
                    .map(ProjectURI::parse)
                    .map(ProjectURI::project)
                    .map(p -> new ProjectRecord(placement, p));

            records = Stream.concat(records, recordStream);
        }

        return records;
    }

    protected static List<String> listAllProjectKeysFor(String profile, Placement placement) {
        S3 s3 = new S3(profile);

        URI uri = ProjectURI.builder()
                .withPlacement(placement)
                .build().uriPrefix();

        S3.Response response = s3.listObjects(uri);

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to list projects in: %s, %s", uri, r.errorMessage())
        );

        return s3.listChildren(response);
    }

    protected List<Placement> listAllPlacements(String profile) {
        S3 s3 = new S3(profile);

        S3.Response response = s3.list(); // list all buckets

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to list buckets, %s", r.errorMessage())
        );

        List<String> list = s3.list(response);

        return Stores.parseBootstrapStoreNames(StateStore.Meta, list);
    }

    @NotNull
    protected List<Placement> filterPlacements(ReportCommandOptions commandOptions) {
        String profile = commandOptions.profile();
        String stage = commandOptions.stage();
        String account = commandOptions.account();
        String region = commandOptions.region();

        return listAllPlacements(profile).stream()
                .filter(p -> stage == null || Objects.equals(p.stage(), Strings.emptyToNull(stage)))
                .filter(p -> account == null || Objects.equals(p.account(), account))
                .filter(p -> region == null || Objects.equals(p.region(), region))
                .map(p -> p.withProvider("aws"))
                .toList();
    }
}
