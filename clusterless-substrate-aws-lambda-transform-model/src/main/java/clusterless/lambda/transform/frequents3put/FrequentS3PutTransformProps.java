/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform.frequents3put;

import clusterless.lambda.transform.TransformProps;
import clusterless.model.deploy.Dataset;
import clusterless.substrate.uri.ManifestURI;

public class FrequentS3PutTransformProps extends TransformProps {
    String sqsQueueName;

    public static Builder builder() {
        return Builder.builder();
    }

    public String sqsQueueName() {
        return sqsQueueName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FrequentS3PutTransformProps{");
        sb.append("sqsQueueName='").append(sqsQueueName).append('\'');
        sb.append(", lotUnit='").append(lotUnit).append('\'');
        sb.append(", manifestCompletePath=").append(manifestCompletePath);
        sb.append(", manifestPartialPath=").append(manifestPartialPath);
        sb.append(", dataset=").append(dataset);
        sb.append(", eventBusName='").append(eventBusName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder {
        String lotUnit;
        ManifestURI manifestCompletePath;
        ManifestURI manifestPartialPath;
        Dataset dataset;
        String eventBusName;
        String sqsQueueName;

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

        public Builder withSqsQueueName(String sqsQueueName) {
            this.sqsQueueName = sqsQueueName;
            return this;
        }

        public FrequentS3PutTransformProps build() {
            FrequentS3PutTransformProps frequentS3PutTransformProps = new FrequentS3PutTransformProps();
            frequentS3PutTransformProps.lotUnit = this.lotUnit;
            frequentS3PutTransformProps.manifestCompletePath = this.manifestCompletePath;
            frequentS3PutTransformProps.eventBusName = this.eventBusName;
            frequentS3PutTransformProps.dataset = this.dataset;
            frequentS3PutTransformProps.manifestPartialPath = this.manifestPartialPath;
            frequentS3PutTransformProps.sqsQueueName = this.sqsQueueName;
            return frequentS3PutTransformProps;
        }
    }
}
