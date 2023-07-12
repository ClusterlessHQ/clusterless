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
    protected String sqsQueueName;
    protected int sqsWaitTimeSeconds = 0;

    public static Builder builder() {
        return Builder.builder();
    }

    public String sqsQueueName() {
        return sqsQueueName;
    }

    public int sqsWaitTimeSeconds() {
        return sqsWaitTimeSeconds;
    }

    @Override
    public String toString() {
        String sb = "FrequentS3PutTransformProps{" + "sqsQueueName='" + sqsQueueName + '\'' +
                    ", sqsWaitTimeSeconds=" + sqsWaitTimeSeconds +
                    ", lotUnit='" + lotUnit + '\'' +
                    ", manifestCompletePath=" + manifestCompletePath +
                    ", manifestPartialPath=" + manifestPartialPath +
                    ", dataset=" + dataset +
                    ", eventBusName='" + eventBusName + '\'' +
                    '}';
        return sb;
    }

    public static final class Builder {
        protected String lotUnit;
        protected ManifestURI manifestCompletePath;
        protected ManifestURI manifestPartialPath;
        protected Dataset dataset;
        protected String eventBusName;
        protected String sqsQueueName;
        protected int sqsWaitTimeSeconds = 0;

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

        public Builder withSqsWaitTimeSeconds(int sqsWaitTimeSeconds) {
            this.sqsWaitTimeSeconds = sqsWaitTimeSeconds;
            return this;
        }

        public FrequentS3PutTransformProps build() {
            FrequentS3PutTransformProps frequentS3PutTransformProps = new FrequentS3PutTransformProps();
            frequentS3PutTransformProps.dataset = this.dataset;
            frequentS3PutTransformProps.sqsQueueName = this.sqsQueueName;
            frequentS3PutTransformProps.manifestCompletePath = this.manifestCompletePath;
            frequentS3PutTransformProps.lotUnit = this.lotUnit;
            frequentS3PutTransformProps.sqsWaitTimeSeconds = this.sqsWaitTimeSeconds;
            frequentS3PutTransformProps.manifestPartialPath = this.manifestPartialPath;
            frequentS3PutTransformProps.eventBusName = this.eventBusName;
            return frequentS3PutTransformProps;
        }
    }
}
