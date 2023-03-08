package clusterless.lambda.manifest;

import clusterless.json.JSONUtil;
import clusterless.model.manifest.Manifest;
import clusterless.substrate.aws.sdk.S3;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

public class ManifestReader {
    protected static final S3 s3 = new S3();

    protected final ObjectReader manifestReader;

    public ManifestReader() {
        manifestReader = JSONUtil.objectReaderFor(Manifest.class);
    }

    public Manifest getManifest(URI manifest) {
        S3.Response response = s3.get(manifest);

        if (!s3.exists(response)) {
            throw new IllegalStateException("manifest not found: " + manifest, response.exception());
        }

        try {
            return manifestReader.readValue(response.inputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
