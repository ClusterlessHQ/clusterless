/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform.s3put;

import clusterless.lambda.transform.TransformProps;
import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.partial.PathFilter;
import clusterless.substrate.uri.ManifestURI;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class S3PutTransformProps extends TransformProps {
    @JsonProperty(required = true)
    LotSource lotSource;
    String keyRegex;

    public S3PutTransformProps() {
    }

    public static Builder builder() {
        return Builder.builder();
    }

    public LotSource lotSource() {
        return lotSource;
    }

    public String keyRegex() {
        return keyRegex;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("S3PutTransformProps{");
        sb.append("lotSource=").append(lotSource);
        sb.append(", keyRegex='").append(keyRegex).append('\'');
        sb.append(", lotUnit='").append(lotUnit).append('\'');
        sb.append(", manifestCompletePath=").append(manifestCompletePath);
        sb.append(", manifestPartialPath=").append(manifestPartialPath);
        sb.append(", dataset=").append(dataset);
        sb.append(", eventBusName='").append(eventBusName).append('\'');
        sb.append(", filter=").append(filter);
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder {
        protected String lotUnit;
        protected ManifestURI manifestCompletePath;
        protected ManifestURI manifestPartialPath;
        protected Dataset dataset;
        protected String eventBusName;
        protected PathFilter filter;
        LotSource lotSource;
        String keyRegex;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withLotUnit(String lotUnit) {
            this.lotUnit = lotUnit;
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

        public Builder withFilter(PathFilter filter) {
            this.filter = filter;
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

        public S3PutTransformProps build() {
            S3PutTransformProps s3PutTransformProps = new S3PutTransformProps();
            s3PutTransformProps.filter = this.filter;
            s3PutTransformProps.lotSource = this.lotSource;
            s3PutTransformProps.dataset = this.dataset;
            s3PutTransformProps.keyRegex = this.keyRegex;
            s3PutTransformProps.lotUnit = this.lotUnit;
            s3PutTransformProps.manifestCompletePath = this.manifestCompletePath;
            s3PutTransformProps.manifestPartialPath = this.manifestPartialPath;
            s3PutTransformProps.eventBusName = this.eventBusName;
            return s3PutTransformProps;
        }
    }
}
