package clusterless.lambda.manifest;

import clusterless.model.UriType;
import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.manifest.Manifest;
import clusterless.substrate.aws.URIFormats;
import clusterless.substrate.aws.sdk.S3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManifestWriter {
    private static final Logger LOG = LogManager.getLogger(ManifestWriter.class);

    protected static final S3 s3 = new S3();
    private final URI manifestPath;
    private final Dataset dataset;
    private final UriType uriType;

    public ManifestWriter(URI manifestPath, Dataset dataset, UriType uriType) {
        this.manifestPath = manifestPath;
        this.dataset = dataset;
        this.uriType = uriType;
    }

    public static Map<String, ManifestWriter> writers(Map<String, SinkDataset> sinks, Map<String, URI> sinkManifestPaths, UriType uriType) {
        Map<String, ManifestWriter> results = new HashMap<>();

        for (Map.Entry<String, SinkDataset> entry : sinks.entrySet()) {
            String role = entry.getKey();
            SinkDataset sinkDataset = entry.getValue();
            results.put(role, new ManifestWriter(sinkManifestPaths.get(role), sinkDataset, uriType));
        }

        return results;
    }

    public URI putManifest(List<URI> uris, String lotId) {
        Manifest manifest = Manifest.Builder.builder()
                .withLotId(lotId)
                .withUriType(uriType)
                .withDataset(dataset)
                .withUris(uris)
                .build();

        // put manifest, nested under the 'lot' partition
        URI manifestURI = URIFormats.createManifestIdentifier(manifestPath, lotId, manifest.extension());

        // todo: perform a listing to test for states (completed, empty, etc)
        S3.Response exists = s3.exists(manifestURI);

        if (exists.isSuccess()) {
            String message = String.format("manifest already exists: %s, having lot: %s", manifestURI, lotId);
            LOG.error(message);
            throw new ManifestExistsException(message);
        }

        LOG.info("writing {} to path: {}", () -> manifest.getClass().getSimpleName(), () -> manifestURI);

        S3.Response response = s3.put(manifestURI, manifest.contentType(), manifest);

        if (!response.isSuccess()) {
            String message = String.format("unable to write object: %s, %s", manifestURI, response.errorMessage());
            LOG.error(message, response.errorMessage());

            throw new RuntimeException(message, response.exception());
        }

        return manifestURI;
    }
}
