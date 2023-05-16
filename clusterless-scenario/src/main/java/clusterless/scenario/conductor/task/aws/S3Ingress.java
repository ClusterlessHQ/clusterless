/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.task.aws;

import clusterless.json.JSONUtil;
import clusterless.scenario.conductor.task.BaseSimpleTask;
import clusterless.scenario.model.IngressStore;

public class S3Ingress extends BaseSimpleTask {

    public static final String AWS_S_3_INGRESS = "awsS3Ingress";

    public S3Ingress(String taskReferenceName, IngressStore store) {
        super(AWS_S_3_INGRESS, taskReferenceName);

        input("input", JSONUtil.writeAsStringSafe(store));
        input("delay", store.uploadDelaySec());
    }
}
