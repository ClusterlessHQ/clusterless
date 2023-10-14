/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.model.deploy;

import clusterless.cls.model.Struct;

/**
 *
 */
public class Workload<P extends WorkloadProps> implements Struct {
    P workloadProps;

    public Workload(P workloadProps) {
        this.workloadProps = workloadProps;
    }

    public P workloadProps() {
        return workloadProps;
    }
}
