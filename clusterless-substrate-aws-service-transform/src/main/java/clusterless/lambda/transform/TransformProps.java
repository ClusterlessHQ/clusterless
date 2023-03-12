/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.model.Struct;
import clusterless.model.deploy.Dataset;
import clusterless.substrate.aws.uri.ManifestURI;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class TransformProps implements Struct {
    @JsonProperty(required = true)
    String lotUnit;

    @JsonProperty(required = true)
    LotSource lotSource;

    String keyRegex;

    @JsonProperty(required = true)
    ManifestURI manifestCompletePath;

    @JsonProperty(required = true)
    ManifestURI manifestPartialPath;

    @JsonProperty(required = true)
    Dataset dataset;

    String eventBusName;

    public TransformProps() {
    }

    public static Builder builder() {
        return Builder.aTransformProps();
    }

    public String lotUnit() {
        return lotUnit;
    }

    public LotSource lotSource() {
        return lotSource;
    }

    public String keyRegex() {
        return keyRegex;
    }

    public ManifestURI manifestCompletePath() {
        return manifestCompletePath;
    }

    public ManifestURI manifestPartialPath() {
        return manifestPartialPath;
    }

    public Dataset dataset() {
        return dataset;
    }

    public String eventBusName() {
        return eventBusName;
    }

    public static final class Builder {
        String lotUnit;
        LotSource lotSource;
        String keyRegex;
        ManifestURI manifestCompletePath;
        ManifestURI manifestPartialPath;
        Dataset dataset;
        String eventBusName;

        private Builder() {
        }

        public static Builder aTransformProps() {
            return new Builder();
        }

        public Builder withLotUnit(String lotUnit) {
            this.lotUnit = lotUnit;
            return this;
        }

        public Builder withLotSource(LotSource lotSource) {
            this.lotSource = lotSource;
            return this;
        }

        public Builder withKeyRegex(String keyRegex) {
            this.keyRegex = keyRegex;
            return this;
        }

        public Builder withManifestCompletePath(ManifestURI manifestCompletePath) {
            this.manifestCompletePath = manifestCompletePath;
            return this;
        }

        public Builder withManifestPartialPath(ManifestURI manifestPartialPath) {
            this.manifestPartialPath = manifestPartialPath;
            return this;
        }

        public Builder withDataset(Dataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public Builder withEventBusName(String eventBusName) {
            this.eventBusName = eventBusName;
            return this;
        }

        public TransformProps build() {
            TransformProps transformProps = new TransformProps();
            transformProps.keyRegex = this.keyRegex;
            transformProps.eventBusName = this.eventBusName;
            transformProps.lotUnit = this.lotUnit;
            transformProps.lotSource = this.lotSource;
            transformProps.dataset = this.dataset;
            transformProps.manifestCompletePath = this.manifestCompletePath;
            transformProps.manifestPartialPath = this.manifestPartialPath;
            return transformProps;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransformProps{");
        sb.append("lotUnit='").append(lotUnit).append('\'');
        sb.append(", lotSource=").append(lotSource);
        sb.append(", keyRegex='").append(keyRegex).append('\'');
        sb.append(", manifestCompletePath=").append(manifestCompletePath);
        sb.append(", manifestPartialPath=").append(manifestPartialPath);
        sb.append(", dataset=").append(dataset);
        sb.append(", eventBusName='").append(eventBusName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
