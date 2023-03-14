package clusterless.lambda.manifest;

import clusterless.model.UriType;
import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.manifest.Manifest;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.sdk.S3;
import clusterless.substrate.aws.uri.ManifestURI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManifestWriter {
    private static final Logger LOG = LogManager.getLogger(ManifestWriter.class);

    protected static final S3 s3 = new S3();
    private final ManifestURI sinkManifestPath;
    private final Dataset sinkDataset;
    private final UriType uriType;

    public static Map<String, ManifestWriter> writers(Map<String, SinkDataset> sinks, Map<String, ManifestURI> sinkManifestPaths, UriType uriType) {
        Map<String, ManifestWriter> results = new HashMap<>();

        for (Map.Entry<String, SinkDataset> entry : sinks.entrySet()) {
            String role = entry.getKey();
            SinkDataset sinkDataset = entry.getValue();
            results.put(role, new ManifestWriter(
                    sinkManifestPaths.get(role),
                    sinkDataset,
                    uriType
            ));
        }

        return results;
    }

    public ManifestWriter(ManifestURI sinkManifestPath, Dataset sinkDataset, UriType uriType) {
        this.sinkManifestPath = sinkManifestPath;
        this.sinkDataset = new Dataset(sinkDataset);
        this.uriType = uriType;
    }

    public URI writeSuccessManifest(List<URI> uris, String lotId) {
        return writeManifest(uris, lotId, sinkManifestPath, ManifestState.complete, null);
    }

    public URI writePartialManifest(List<URI> uris, String lotId, String comment) {
        return writeManifest(uris, lotId, sinkManifestPath, ManifestState.partial, comment);
    }

    public URI writeEmptyManifest(List<URI> uris, String lotId) {
        return writeManifest(uris, lotId, sinkManifestPath, ManifestState.empty, null);
    }

    private URI writeManifest(List<URI> uris, String lotId, ManifestURI sinkManifestPath, ManifestState state, String comment) {
        Manifest manifest = Manifest.builder()
                .withState(state)
                .withComment(comment)
                .withLotId(lotId)
                .withUriType(uriType)
                .withDataset(sinkDataset)
                .withUris(uris)
                .build();

        // put manifest, nested under the 'lot' partition
        URI sinkManifestIdentifier = sinkManifestPath.withState(state).withLot(lotId).uri();

        LOG.info("testing manifest: {}", sinkManifestIdentifier);

        // todo: perform a listing to test for states (completed, empty, etc)
        S3.Response exists = s3.exists(sinkManifestIdentifier);

        exists.isNotSuccessOrThrow(
                r -> String.format("manifest already exists: %s", sinkManifestIdentifier),
                ManifestExistsException::new
        );

        LOG.info("writing {} to path: {}", () -> manifest.getClass().getSimpleName(), () -> sinkManifestIdentifier);

        S3.Response response = s3.put(sinkManifestIdentifier, manifest.contentType(), manifest);

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to write object: %s, %s", sinkManifestIdentifier, r.errorMessage())
        );

        return sinkManifestIdentifier;
    }
}
