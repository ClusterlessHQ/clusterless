package clusterless.lambda;

import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.deploy.SourceDataset;
import clusterless.model.manifest.Manifest;
import clusterless.substrate.aws.URIFormats;
import clusterless.util.URIs;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestDatasets {

    public static final String MANIFEST_BUCKET = "manifests";
    Map<String, String> roles = new LinkedHashMap<>();

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

    public Map<String, Dataset> datasetMap() {
        return datasetMap(roles);
    }

    public Map<String, SourceDataset> sourceDatasetMap() {
        return sourceDatasetMap(roles);
    }

    public Map<String, SinkDataset> sinkDatasetMap() {
        return sinkDatasetMap(roles);
    }


    public Map<String, URI> manifestIdentifierMap(String lotId) {
        return manifestIdentifierMap(datasetMap(), lotId);
    }

    public Map<String, URI> manifestIdentifierMap(String lotId, Map<String, ? extends Dataset> datasetMap) {
        return manifestIdentifierMap(datasetMap, lotId);
    }

    public Map<String, URI> sourceManifestPathMap() {
        return manifestPathMap(sourceDatasetMap());
    }

    public Map<String, URI> sinkManifestPathMap() {
        return manifestPathMap(sinkDatasetMap());
    }

    public Map<String, URI> manifestPathMap() {
        return manifestPathMap(datasetMap());
    }

    public List<Dataset> datasetList() {
        return datasets(roles).collect(Collectors.toList());
    }

    public List<URI> manifestPathList() {
        return manifestPaths(roles).collect(Collectors.toList());
    }

    public Stream<Dataset> datasets() {
        return datasets(roles);
    }

    public static Map<String, URI> manifestPathMap(Map<String, ? extends Dataset> datasetMap) {
        return datasetMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> manifestPathURIFor(e.getValue().name())));
    }

    public static Map<String, URI> manifestIdentifierMap(Map<String, ? extends Dataset> datasetMap, String lotId) {
        return manifestPathMap(datasetMap).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> URIFormats.createManifestIdentifier(e.getValue(), lotId, Manifest.JSON_EXTENSION)));
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

    public static Stream<URI> manifestPaths(Map<String, String> roles) {
        return roles.values().stream().map(
                TestDatasets::manifestPathURIFor
        );
    }

    public static Stream<Dataset> datasets(int size) {
        return IntStream.range(0, size).mapToObj(
                i -> datasetFor(name(i))
        );
    }

    public static Stream<URI> manifestPaths(int size) {
        return IntStream.range(0, size).mapToObj(
                i -> manifestPathURIFor(name(i))
        );
    }

    @NotNull
    public static Dataset datasetFor(String name) {
        return Dataset.Builder.builder()
                .withName(name)
                .withVersion(version())
                .withPathURI(path(name))
                .build();
    }

    @NotNull
    public static SourceDataset sourceDatasetFor(String name) {
        String qualified = String.format("source-%s", name);
        return SourceDataset.Builder.builder()
                .withName(qualified)
                .withVersion(version())
                .withPathURI(path(qualified))
                .build();
    }

    @NotNull
    public static SinkDataset sinkDatasetFor(String name) {
        String qualified = String.format("sink-%s", name);
        return SinkDataset.Builder.builder()
                .withName(qualified)
                .withVersion(version())
                .withPathURI(path(qualified))
                .build();
    }

    @NotNull
    public static URI manifestPathURIFor(String name) {
        return URIs.copyAppendPath(URI.create("s3://" + MANIFEST_BUCKET + "/"), name, version());
    }

    @NotNull
    public static String role(int i) {
        return String.format("role-%2d", i);
    }

    @NotNull
    public static URI path(String path) {
        return URI.create(String.format("s3://bucket-%s/path/", path));
    }

    public static String version() {
        return "20230101";
    }

    public static String name(int i) {
        return String.format("dataset-%02d", i);
    }
}
