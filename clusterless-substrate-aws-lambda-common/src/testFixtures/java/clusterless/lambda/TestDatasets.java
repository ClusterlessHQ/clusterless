/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda;

import clusterless.model.deploy.*;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.uri.ManifestURI;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestDatasets {
    public static Placement defaultPlacement = Placement.builder()
            .withAccount("00000000")
            .withRegion("us-west-2")
            .withProvider("aws")
            .withStage("prod")
            .build();

    public static Project project = Project.Builder.builder()
            .withName("test-project")
            .withVersion("20230101")
            .build();
    Map<String, String> roles;

    public Placement placement = defaultPlacement;

    public TestDatasets() {
        this(1);
    }

    public TestDatasets(String... roles) {
        this(Arrays.asList(roles));
    }

    public TestDatasets(List<String> roles) {
        final int[] count = {0};
        this.roles = roles.stream().collect(Collectors.toMap(r -> r, r -> name(count[0]++)));
    }

    public TestDatasets(int size) {
        roles = IntStream.range(0, size).boxed().collect(Collectors.toMap(TestDatasets::role, TestDatasets::name));
    }

    public TestDatasets(Placement placement, String... roles) {
        this(roles);
        Objects.requireNonNull(placement, "placement is null");
        this.placement = placement;
    }

    public Map<String, LocatedDataset> datasetMap() {
        return datasetMap(roles);
    }

    public Map<String, LocatedDataset> sourceDatasetMap() {
        return sourceDatasetMap(roles);
    }

    public Map<String, SourceDataset> sourceDatasetMapAsSource() {
        return sourceDatasetMap().entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), new SourceDataset(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, LocatedDataset> sinkDatasetMap() {
        return sinkDatasetMap(roles);
    }

    public Map<String, SinkDataset> sinkDatasetMapAsSink() {
        return sinkDatasetMap().entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), new SinkDataset(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, ManifestURI> manifestIdentifierMap(String lotId, ManifestState state) {
        return manifestIdentifierMap(lotId, datasetMap(), state);
    }

    public Map<String, ManifestURI> manifestIdentifierMap(String lotId, Map<String, ? extends LocatedDataset> datasetMap, ManifestState state) {
        return manifestIdentifierMap(datasetMap, lotId, state);
    }

    public Map<String, ManifestURI> sourceManifestPathMap() {
        return manifestPathMap(sourceDatasetMap(), null);
    }

    public Map<String, ManifestURI> sinkManifestPathMap() {
        return manifestPathMap(sinkDatasetMap(), null);
    }

    public Map<String, ManifestURI> manifestPathMap() {
        return manifestPathMap(datasetMap(), null);
    }

    public List<SinkDataset> sinkDatasetListAsSink() {
        return sinkDatasets(roles).map(SinkDataset::new).collect(Collectors.toList());
    }

    public List<ManifestURI> manifestPathList(ManifestState state) {
        return manifestPaths(roles, state).collect(Collectors.toList());
    }

    public Stream<LocatedDataset> datasets() {
        return datasets(roles);
    }

    public Map<String, ManifestURI> manifestPathMapFor(Map<String, ? extends LocatedDataset> datasetMap) {
        return manifestPathMap(datasetMap, null);
    }

    public Map<String, ManifestURI> manifestPathMap(Map<String, ? extends LocatedDataset> datasetMap, ManifestState state) {
        return datasetMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ManifestURI.builder()
                        .withPlacement(placement)
                        .withDataset(e.getValue())
                        .withState(state)
                        .build()));
    }

    public Map<String, ManifestURI> manifestIdentifierMap(Map<String, ? extends LocatedDataset> datasetMap, String lotId, ManifestState state) {
        return datasetMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ManifestURI.builder()
                        .withPlacement(placement)
                        .withDataset(e.getValue())
                        .withLotId(lotId)
                        .withState(state)
                        .build()));
    }

    public static Map<String, LocatedDataset> datasetMap(Map<String, String> roles) {
        return roles.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> datasetFor(e.getValue())));
    }

    public static Map<String, LocatedDataset> sourceDatasetMap(Map<String, String> roles) {
        return roles.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> sourceDatasetFor(e.getValue())));
    }

    public static Map<String, LocatedDataset> sinkDatasetMap(Map<String, String> roles) {
        return roles.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> sinkDatasetFor(e.getValue())));
    }

    public static Stream<LocatedDataset> datasets(Map<String, String> roles) {
        return roles.values().stream().map(
                TestDatasets::datasetFor
        );
    }

    public static Stream<LocatedDataset> sinkDatasets(Map<String, String> roles) {
        return roles.values().stream().map(
                TestDatasets::sinkDatasetFor
        );
    }

    public Stream<ManifestURI> manifestPaths(Map<String, String> roles, ManifestState state) {
        return roles.values().stream().map(e ->
                ManifestURI.builder()
                        .withPlacement(placement)
                        .withDataset(datasetFor(e))
                        .withState(state)
                        .build()
        );
    }

    public static Stream<LocatedDataset> datasets(int size) {
        return IntStream.range(0, size).mapToObj(
                i -> datasetFor(name(i))
        );
    }

    @NotNull
    public static LocatedDataset datasetFor(String name) {
        return LocatedDataset.Builder.builder()
                .withName(name)
                .withVersion(version())
                .withPathURI(datasetPath(name))
                .build();
    }

    @NotNull
    public static LocatedDataset sourceDatasetFor(String name) {
        return locatedDatasetFor("source", name);
    }

    @NotNull
    public static LocatedDataset sinkDatasetFor(String name) {
        return locatedDatasetFor("sink", name);
    }

    @NotNull
    public static LocatedDataset locatedDatasetFor(String prefix, String name) {
        String qualified = String.format("%s-%s", prefix, name);
        return SinkDataset.Builder.builder()
                .withName(qualified)
                .withVersion(version())
                .withPathURI(datasetPath(qualified))
                .build();
    }

    @NotNull
    public static String role(int i) {
        return String.format("role-%2d", i);
    }

    @NotNull
    public static URI datasetPath(String path) {
        return URI.create(String.format("s3://dataset-bucket-%s/path/", path));
    }

    public static String version() {
        return "20230101";
    }

    public static String name(int i) {
        return String.format("dataset-%02d", i);
    }
}
