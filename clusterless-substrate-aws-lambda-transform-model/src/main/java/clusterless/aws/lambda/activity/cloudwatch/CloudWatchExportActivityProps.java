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
    int timeoutMin;

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

    public int timeoutMin() {
        return timeoutMin;
    }

    public static final class Builder {
        String interval;
        String logGroupName;
        String logStreamPrefix;
        URI pathURI;
        int timeoutMin;

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

        public Builder withTimeoutMin(int timeoutMin) {
            this.timeoutMin = timeoutMin;
            return this;
        }

        public CloudWatchExportActivityProps build() {
            CloudWatchExportActivityProps cloudWatchExportActivityProps = new CloudWatchExportActivityProps();
            cloudWatchExportActivityProps.logStreamPrefix = this.logStreamPrefix;
            cloudWatchExportActivityProps.logGroupName = this.logGroupName;
            cloudWatchExportActivityProps.pathURI = this.pathURI;
            cloudWatchExportActivityProps.timeoutMin = this.timeoutMin;
            cloudWatchExportActivityProps.interval = this.interval;
            return cloudWatchExportActivityProps;
        }
    }
}
