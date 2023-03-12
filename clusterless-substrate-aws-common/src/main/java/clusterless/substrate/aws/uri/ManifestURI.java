package clusterless.substrate.aws.uri;

import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.Placement;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.store.StateStore;
import clusterless.util.Lazy;
import clusterless.util.Partition;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;

import static clusterless.util.Optionals.optional;
import static java.util.Optional.ofNullable;

/**
 * Identifier
 * <p>
 * {@code {provider-service}://{manifest-store}/{dataset-name}/{dataset-version}/{lot}/{state}[/{attempt}]/manifest.{ext}}
 * or
 * {@code {provider-service}://{manifest-store}/{dataset-name}/{dataset-version}/{lot}/{state}[/{attempt}]/manifest-root.{ext}}
 * {@code {provider-service}://{manifest-store}/{dataset-name}/{dataset-version}/{lot}/{state}[/{attempt}]/{part-a}[/{part-b}/...]/manifest.{ext}}
 * <p>
 * Path
 * {@code {provider-service}://{manifest-store}/{dataset-name}/{dataset-version}/
 */
@JsonSerialize(using = ManifestURI.Serializer.class)
@JsonDeserialize(using = ManifestURI.DeSerializer.class)
public class ManifestURI extends StateURI<ManifestState, ManifestURI> {

    static class Serializer extends StdScalarSerializer<ManifestURI> {
        protected Serializer() {
            super(ManifestURI.class);
        }

        @Override
        public void serialize(ManifestURI value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.template());
        }
    }

    static class DeSerializer extends StdScalarDeserializer<ManifestURI> {
        protected DeSerializer() {
            super(ManifestURI.class);
        }

        @Override
        public ManifestURI deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            // The critical path: ensure we handle the common case first.
            if (p.hasToken(JsonToken.VALUE_STRING)) {
                return ManifestURI.parse(p.getText());
            }
            // [databind#381]
//            if (p.hasToken(JsonToken.START_ARRAY)) {
//                return _deserializeFromArray(p, ctxt);
//            }

            return ManifestURI.parse(_parseString(p, ctxt, this));
        }
    }

    // todo: make attempt supplier a strategy class/enum so that we can json serialize it
    protected transient Supplier<String> attemptSupplier = Lazy.of(() -> String.valueOf(System.currentTimeMillis()));

    protected Dataset dataset;

    protected ManifestURI() {
        super(StateStore.Manifest);
    }

    protected ManifestURI(ManifestURI other) {
        super(other);
        this.dataset = other.dataset;
        this.attemptSupplier = other.attemptSupplier;
    }

    public static Builder builder() {
        return Builder.aManifestStateIdentifier();
    }

    protected ManifestURI copy() {
        return new ManifestURI(this);
    }

    @Override
    public boolean isPath() {
        return state == null;
    }

    public static ManifestURI parse(String template) {
        Objects.requireNonNull(template, "template is null");

        // {provider-service}://{manifest-store}/datasets/{dataset-name}/{dataset-version}/{lot}/{state}[/{attempt}]/manifest.{ext}
        String[] split = template.split("/");

        int index = 4; // start after arcs
        return new ManifestURI()
                .setStoreName(value(split, 2))
                .setDataset(Dataset.Builder.builder()
                        .withName(value(split, index++))
                        .withVersion(value(split, index++))
                        .build())
                .setLotId(value(split, index++))
                .setState(ManifestState.parse(optional(index++, split).orElse(null)))
                .setAttemptId(value(split, index++));
    }

    @Override
    public URI uri() {
        require(dataset, "dataset");
        require(lotId == null && state != null, "lotId is required if state is set");

        Partition manifest = ofNullable(state).map(s -> (Partition) s).orElse(Partition.NULL);

        if (!manifest.isNull()) {
            Partition attempt = state.hasAttempts() ? Partition.namedOf("attempt", attemptId()) : Partition.NULL;

            manifest = manifest
                    .with(attempt)
                    .with("manifest.json");
        }

        String path = Partition.of("datasets")
                .withNamed("name", dataset.name())
                .withNamed("version", dataset.version())
                .withNamed("lot", lotId) // retain case
                .pathUnless(manifest);

        return createUri(path);
    }

    @Override
    public String template() {
        // {provider-service}://{manifest-store}/datasets/{dataset-name}/{dataset-version}/{lot}/{state}[/{attempt}]/manifest.{ext}

        // bypass the named partition and store directly
        Partition manifest = ofNullable(state).map(s -> (Partition) s).orElse(Partition.namedOf("state", "{state}"));

        Partition attempt = state == null ?
                Partition.of("attempt={attempt}") :
                state.hasAttempts() ? Partition.namedOf("attempt", attemptId()) : Partition.NULL;

        manifest = manifest
                .with(attempt)
                .with("manifest.json");

        String path = Partition.namedOf("name", ofNullable(dataset.name()).orElse("{datasetName}"))
                .withNamed("version", ofNullable(dataset.version()).orElse("{datasetVersion}"))
                .withNamed("lot", ofNullable(lotId).orElse("{lot}")) // retain case
                .with(manifest)
                .partition();

        return String.format("s3://%s/datasets/%s", storeName.get(), path);
    }

    @JsonIgnore
    protected String attemptId() {
        return attemptSupplier.get();
    }

    protected ManifestURI setAttemptId(String attemptId) {
        this.attemptSupplier = () -> attemptId;
        return this;
    }


    protected ManifestURI setDataset(Dataset dataset) {
        this.dataset = dataset;
        return this;
    }

    public ManifestURI withDataset(Dataset dataset) {
        return copy().setDataset(dataset);
    }

    @Override
    public ManifestURI self() {
        return this;
    }

    public static final class Builder {
        private Dataset dataset;
        private Placement placement;
        private String lotId;
        private ManifestState state;

        private Builder() {
        }

        public static Builder aManifestStateIdentifier() {
            return new Builder();
        }

        public Builder withDataset(Dataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public Builder withPlacement(Placement placement) {
            this.placement = placement;
            return this;
        }

        public Builder withLotId(String lotId) {
            this.lotId = lotId;
            return this;
        }

        public Builder withState(ManifestState state) {
            this.state = state;
            return this;
        }

        public ManifestURI build() {
            ManifestURI manifestStateIdentifier = new ManifestURI();
            manifestStateIdentifier.setDataset(dataset);
            manifestStateIdentifier.setPlacement(placement);
            manifestStateIdentifier.setLotId(lotId);
            manifestStateIdentifier.setState(state);
            return manifestStateIdentifier;
        }
    }
}
