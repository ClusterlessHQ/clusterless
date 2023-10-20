/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.uri;

import clusterless.cls.model.deploy.Dataset;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.substrate.store.StateStore;
import clusterless.commons.naming.Partition;
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
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Path
 * <p>
 * {@code {providerService}://{stateStore}/{datasetName}/{datasetVersion}/}
 * <p>
 * Identifier
 * <p>
 * {@code {providerService}://{stateStore}/{datasetName}/{datasetVersion}/dataset.json}
 */
@JsonSerialize(using = DatasetURI.Serializer.class)
@JsonDeserialize(using = DatasetURI.DeSerializer.class)
public class DatasetURI extends MetaURI<Dataset, DatasetURI> {

    public static final String DATASETS = "datasets";

    public static Builder builder() {
        return Builder.builder();
    }

    static class Serializer extends StdScalarSerializer<DatasetURI> {
        protected Serializer() {
            super(DatasetURI.class);
        }

        @Override
        public void serialize(DatasetURI value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.template());
        }
    }

    static class DeSerializer extends StdScalarDeserializer<DatasetURI> {
        protected DeSerializer() {
            super(DatasetURI.class);
        }

        @Override
        public DatasetURI deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            // The critical path: ensure we handle the common case first.
            if (p.hasToken(JsonToken.VALUE_STRING)) {
                return DatasetURI.parse(p.getText());
            }
            // [databind#381]
//            if (p.hasToken(JsonToken.START_ARRAY)) {
//                return _deserializeFromArray(p, ctxt);
//            }

            return DatasetURI.parse(_parseString(p, ctxt, this));
        }
    }

    protected Dataset dataset;

    protected DatasetURI() {
        super(StateStore.Meta);
    }

    protected DatasetURI(DatasetURI other) {
        super(other);
        this.dataset = other.dataset;
    }

    public Dataset dataset() {
        return dataset;
    }

    protected DatasetURI copy() {
        return new DatasetURI(this);
    }

    @Override
    public boolean isPath() {
        return dataset == null || dataset.name() == null || dataset.version() == null;
    }

    /**
     * Template may be a URI or an absolute path.
     *
     * @param template
     * @return
     */
    public static DatasetURI parse(String template) {
        Objects.requireNonNull(template, "template is null");

        // {providerService}://{stateStore}/datasets/{datasetName}/{datasetVersion}/dataset.json
        String[] split = template.split("/");

        boolean isOnlyPath = isOnlyPath(template);
        int index = isOnlyPath ? 2 : 4;
        String storeName = isOnlyPath ? null : value(split, 2);
        return new DatasetURI()
                .setStoreName(storeName) // the bucket in s3
                .setDataset(Dataset.Builder.builder()
                        .withName(value(split, index++))
                        .withVersion(value(split, index))
                        .build());
    }

    @Override
    public URI uriPrefix() {
        Partition partition = Partition.of(DATASETS)
                .withNamedTerminal("name", ofNullable(dataset).map(Dataset::name))
                .withNamedTerminal("version", ofNullable(dataset).map(Dataset::version));

        return createUri(partition.prefix());
    }

    @Override
    public URI uriPath() {
        String path = Partition.of(DATASETS)
                .withNamedTerminal("name", Optional.ofNullable(dataset).map(Dataset::name))
                .withNamedTerminal("version", Optional.ofNullable(dataset).map(Dataset::version))
                .with("dataset.json")
                .prefix();

        return createUri(path);
    }

    @Override
    public URI uri() {
        require(dataset, "dataset");

        String path = Partition.of(DATASETS)
                .withNamed("name", dataset.name())
                .withNamed("version", dataset.version())
                .with("dataset.json")
                .prefix();

        return createUri(path);
    }

    @Override
    public String template() {
        String path = Partition.namedOf("name", ofNullable(dataset.name()).orElse("{datasetName}"))
                .withNamed("version", ofNullable(dataset.version()).orElse("{datasetVersion}"))
                .with("dataset.json")
                .partition();

        return String.format("s3://%s/%s/%s", storeName.get(), DATASETS, path);
    }

    protected DatasetURI setDataset(Dataset dataset) {
        this.dataset = dataset;
        return this;
    }

    public DatasetURI withDataset(Dataset dataset) {
        return copy().setDataset(dataset);
    }

    @Override
    public DatasetURI self() {
        return this;
    }

    public static final class Builder {
        protected Dataset dataset;
        protected Placement placement;

        private Builder() {
        }

        public static Builder builder() {
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

        public DatasetURI build() {
            DatasetURI datasetURI = new DatasetURI();
            datasetURI.setDataset(dataset);
            datasetURI.setPlacement(placement);
            return datasetURI;
        }
    }
}
