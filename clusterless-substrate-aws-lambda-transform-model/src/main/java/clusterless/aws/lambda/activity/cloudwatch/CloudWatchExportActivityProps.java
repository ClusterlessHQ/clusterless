/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.activity.cloudwatch;

import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.model.Struct;

import java.net.URI;

public class CloudWatchExportActivityProps implements Struct {
    @JsonRequiredProperty
    String interval;
    @JsonRequiredProperty
    String logGroupName;
    String logStreamPrefix;
    @JsonRequiredProperty
    URI pathURI;

    public static Builder builder() {
        return Builder.builder();
    }

    public String interval() {
        return interval;
    }

    public String logGroupName() {
        return logGroupName;
    }

    public String logStreamPrefix() {
        return logStreamPrefix;
    }

    public URI pathURI() {
        return pathURI;
    }

    public static final class Builder {
        String interval;
        String logGroupName;
        String logStreamPrefix;
        URI pathURI;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withInterval(String interval) {
            this.interval = interval;
            return this;
        }

        public Builder withLogGroupName(String logGroupName) {
            this.logGroupName = logGroupName;
            return this;
        }

        public Builder withLogStreamPrefix(String logStreamPrefix) {
            this.logStreamPrefix = logStreamPrefix;
            return this;
        }

        public Builder withPathURI(URI pathURI) {
            this.pathURI = pathURI;
            return this;
        }

        public CloudWatchExportActivityProps build() {
            CloudWatchExportActivityProps cloudWatchExportActivityProps = new CloudWatchExportActivityProps();
            cloudWatchExportActivityProps.pathURI = this.pathURI;
            cloudWatchExportActivityProps.logGroupName = this.logGroupName;
            cloudWatchExportActivityProps.logStreamPrefix = this.logStreamPrefix;
            cloudWatchExportActivityProps.interval = this.interval;
            return cloudWatchExportActivityProps;
        }
    }
}
