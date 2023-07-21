/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.uri;

import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.Placement;
import clusterless.model.manifest.ManifestState;
import clusterless.naming.Partition;
import clusterless.substrate.store.StateStore;
import clusterless.util.Lazy;
import clusterless.util.URIs;
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
import java.net.URLDecoder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static clusterless.util.Optionals.optional;
import static java.nio.charset.StandardCharsets.UTF_8;

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

    public static final String DATASETS = "datasets";

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

    public static ManifestURI parse(URI uri) {
        return parse(uri.toString());
    }

    public static ManifestURI parse(String template) {
        Objects.requireNonNull(template, "template is null");

        template = URLDecoder.decode(template, UTF_8);

        // {provider-service}://{manifest-store}/datasets/{dataset-name}/{dataset-version}/{lot}/{state}[/{attempt}]/manifest.{ext}
        String[] split = template.split("/");

        int index = 4; // start after arcs
        return new ManifestURI()
                .setStoreName(value(split, 2))
                .setDataset(Dataset.builder()
                        .withName(value(split, index++))
                        .withVersion(value(split, index++))
                        .build())
                .setLotId(value(split, index++))
                .setState(ManifestState.parse(optional(index++, split).orElse(null)))
                .setAttemptId(value(split, index++));
    }

    @Override
    public URI uriPrefix() {
        return createUri(terminalPartition().prefix());
    }

    @Override
    public URI uriPath() {
        return createUri(terminalPartition().path());
    }

    private Partition terminalPartition() {
        Partition attempt = state != null && state.hasAttempts() ? Partition.namedOf("attempt", attemptId()) : Partition.NULL;


        return Partition.of(DATASETS)
                .withNamedTerminal("name", Optional.ofNullable(dataset).map(Dataset::name))
                .withNamedTerminal("version", Optional.ofNullable(dataset).map(Dataset::version))
                .withNamedTerminal("lot", lotId) // retain case
                .withTerminal(state)
                .withTerminal(attempt);
    }

    @Override
    public URI uri() {
        require(dataset, "dataset");
        require(lotId == null && state != null, "lotId is required if state is set");

        Partition manifest = Optional.ofNullable(state).map(s -> (Partition) s).orElse(Partition.NULL);

        if (!manifest.isNull()) {
            Partition attempt = state.hasAttempts() ? Partition.namedOf("attempt", attemptId()) : Partition.NULL;

            manifest = manifest
                    .with(attempt)
                    .with("manifest.json");
        }

        String path = Partition.of(DATASETS)
                .withNamed("name", dataset.name())
                .withNamed("version", dataset.version())
                .withNamed("lot", lotId) // retain case
                .pathUnless(manifest);

        return createUri(path);
    }

    @Override
    public String template() {
        // {provider-service}://{manifest-store}/datasets/{dataset-name}/{dataset-version}/{lot}/{state}{/attempt*}]/manifest.{ext}
        String path = templatePath();

        return String.format("s3://%s/%s/%s", storeName.get(), DATASETS, path);
    }

    public String templateEncoded() {
        // {provider-service}://{manifest-store}/datasets/{dataset-name}/{dataset-version}/{lot}/{state}{/attempt*}]/manifest.{ext}
        String path = URIs.encodeOnly("{} ", templatePath());

        return String.format("s3://%s/%s/%s", storeName.get(), DATASETS, path);
    }

    protected String templatePath() {
        // bypass the named partition and store directly
        Partition manifest = Optional.ofNullable(state).map(s -> (Partition) s).orElse(Partition.namedOf("state", "{state}"));

        Partition attempt = state == null ?
                Partition.literal("{/attempt*}") :
                state.hasAttempts() ? Partition.namedOf("attempt", attemptId()) : Partition.NULL;

        manifest = manifest
                .with(attempt)
                .with("manifest.json");

        String path = Partition.namedOf("name", Optional.ofNullable(dataset.name()).orElse("{datasetName}"))
                .withNamed("version", Optional.ofNullable(dataset.version()).orElse("{datasetVersion}"))
                .withNamed("lot", Optional.ofNullable(lotId).orElse("{lot}")) // retain case
                .with(manifest)
                .partition();
        return path;
    }

    @JsonIgnore
    protected String attemptId() {
        return attemptSupplier.get();
    }

    protected ManifestURI setAttemptId(String attemptId) {
        this.attemptSupplier = () -> attemptId;
        return this;
    }

    public Dataset dataset() {
        return dataset;
    }

    protected ManifestURI setDataset(Dataset dataset) {
        this.dataset = dataset;
        return this;
    }

    public ManifestURI withDataset(Dataset dataset) {
        return copy().setDataset(dataset);
    }

    public ManifestURI withAttemptId(String attemptId) {
        return copy().setAttemptId(attemptId);
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
