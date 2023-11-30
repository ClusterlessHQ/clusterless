/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.boundary.frequents3put;

import clusterless.aws.lambda.boundary.BoundaryProps;
import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.model.deploy.partial.PathFilter;
import clusterless.cls.substrate.uri.ManifestURI;

public class FrequentS3PutBoundaryProps extends BoundaryProps {

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
        final StringBuilder sb = new StringBuilder("FrequentS3PutTransformProps{");
        sb.append("sqsQueueName='").append(sqsQueueName).append('\'');
        sb.append(", sqsWaitTimeSeconds=").append(sqsWaitTimeSeconds);
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
        protected SinkDataset dataset;
        protected String eventBusName;
        protected PathFilter filter = new PathFilter();
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

        public Builder withDataset(SinkDataset dataset) {
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

        public Builder withSqsQueueName(String sqsQueueName) {
            this.sqsQueueName = sqsQueueName;
            return this;
        }

        public Builder withSqsWaitTimeSeconds(int sqsWaitTimeSeconds) {
            this.sqsWaitTimeSeconds = sqsWaitTimeSeconds;
            return this;
        }

        public FrequentS3PutBoundaryProps build() {
            FrequentS3PutBoundaryProps frequentS3PutTransformProps = new FrequentS3PutBoundaryProps();
            frequentS3PutTransformProps.eventBusName = this.eventBusName;
            frequentS3PutTransformProps.sqsWaitTimeSeconds = this.sqsWaitTimeSeconds;
            frequentS3PutTransformProps.sqsQueueName = this.sqsQueueName;
            frequentS3PutTransformProps.manifestCompletePath = this.manifestCompletePath;
            frequentS3PutTransformProps.manifestPartialPath = this.manifestPartialPath;
            frequentS3PutTransformProps.dataset = this.dataset;
            frequentS3PutTransformProps.lotUnit = this.lotUnit;
            frequentS3PutTransformProps.filter = this.filter;
            return frequentS3PutTransformProps;
        }
    }
}
