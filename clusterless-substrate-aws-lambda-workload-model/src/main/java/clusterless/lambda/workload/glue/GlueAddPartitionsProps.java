/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.workload.glue;

import clusterless.model.deploy.WorkloadProps;

public class GlueAddPartitionsProps extends WorkloadProps {
    public enum PartitionType {
        named,
        value
    }

    public enum PartitionResults {
        none,
        all,
        added
    }

    PartitionType partitionType = PartitionType.named;
    String namedPartitionDelimiter = "=";

    PartitionResults partitionResults = PartitionResults.added;

    public PartitionType partitionType() {
        return partitionType;
    }

    public String namedPartitionDelimiter() {
        return namedPartitionDelimiter;
    }

    public PartitionResults partitionResults() {
        return partitionResults;
    }
}
