package clusterless.lambda;

import clusterless.model.deploy.*;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.uri.ManifestURI;
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
    public static Placement defaultPlacement = Placement.Builder.builder()
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

    public TestDatasets(Map<String, String> roles) {
        this.roles = roles;
    }

    public TestDatasets(int size) {
        roles = IntStream.range(0, size).boxed().collect(Collectors.toMap(TestDatasets::role, TestDatasets::name));
    }

    public TestDatasets(Placement placement, String... roles) {
        this(roles);
        Objects.requireNonNull(placement, "placement is null");
        this.placement = placement;
    }

    public Map<String, Dataset> datasetMap() {
        return datasetMap(roles);
    }

    public Map<String, SourceDataset> sourceDatasetMap() {
        return sourceDatasetMap(roles);
    }

    public Map<String, SinkDataset> sinkDatasetMap() {
        return sinkDatasetMap(roles);
    }

    public Map<String, ManifestURI> manifestIdentifierMap(String lotId, ManifestState state) {
        return manifestIdentifierMap(lotId, datasetMap(), state);
    }

    public Map<String, ManifestURI> manifestIdentifierMap(String lotId, Map<String, ? extends Dataset> datasetMap, ManifestState state) {
        return manifestIdentifierMap(datasetMap, lotId, state);
    }

    public Map<String, ManifestURI> sourceManifestPathMap() {
        return manifestPathMap(sourceDatasetMap(), null);
    }

    public Map<String, ManifestURI> sourceManifestPathMap(ManifestState state) {
        return manifestPathMap(sourceDatasetMap(), state);
    }

    public Map<String, ManifestURI> sinkManifestPathMap() {
        return manifestPathMap(sinkDatasetMap(), null);
    }

    public Map<String, ManifestURI> sinkManifestPathMap(ManifestState state) {
        return manifestPathMap(sinkDatasetMap(), state);
    }

    public Map<String, ManifestURI> manifestPathMap() {
        return manifestPathMap(datasetMap(), null);
    }

    public List<Dataset> datasetList() {
        return datasets(roles).collect(Collectors.toList());
    }

    public List<ManifestURI> manifestPathList(ManifestState state) {
        return manifestPaths(roles, state).collect(Collectors.toList());
    }

    public Stream<Dataset> datasets() {
        return datasets(roles);
    }

    public Map<String, ManifestURI> manifestPathMap(Map<String, ? extends Dataset> datasetMap, ManifestState state) {
        return datasetMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ManifestURI.builder()
                        .withPlacement(placement)
                        .withDataset(e.getValue())
                        .withState(state)
                        .build()));
    }

    public Map<String, ManifestURI> manifestIdentifierMap(Map<String, ? extends Dataset> datasetMap, String lotId, ManifestState state) {
        return datasetMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ManifestURI.builder()
                        .withPlacement(placement)
                        .withDataset(e.getValue())
                        .withLotId(lotId)
                        .withState(state)
                        .build()));
    }

    public static Map<String, Dataset> datasetMap(Map<String, String> roles) {
        return roles.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> datasetFor(e.getValue())));
    }

    public static Map<String, SourceDataset> sourceDatasetMap(Map<String, String> roles) {
        return roles.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> sourceDatasetFor(e.getValue())));
    }

    public static Map<String, SinkDataset> sinkDatasetMap(Map<String, String> roles) {
        return roles.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> sinkDatasetFor(e.getValue())));
    }

    public static Stream<Dataset> datasets(Map<String, String> roles) {
        return roles.values().stream().map(
                TestDatasets::datasetFor
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

    public static Stream<Dataset> datasets(int size) {
        return IntStream.range(0, size).mapToObj(
                i -> datasetFor(name(i))
        );
    }

    public Stream<ManifestURI> manifestPaths(int size) {
        return IntStream.range(0, size).mapToObj(
                i -> ManifestURI.builder()
                        .withPlacement(placement)
                        .withDataset(datasetFor(name(i)))
                        .build()
        );
    }

    @NotNull
    public static Dataset datasetFor(String name) {
        return Dataset.Builder.builder()
                .withName(name)
                .withVersion(version())
                .withPathURI(datasetPath(name))
                .build();
    }

    @NotNull
    public static SourceDataset sourceDatasetFor(String name) {
        String qualified = String.format("source-%s", name);
        return SourceDataset.Builder.builder()
                .withName(qualified)
                .withVersion(version())
                .withPathURI(datasetPath(qualified))
                .build();
    }

    @NotNull
    public static SinkDataset sinkDatasetFor(String name) {
        String qualified = String.format("sink-%s", name);
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
