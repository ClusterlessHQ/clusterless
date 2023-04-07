/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.model.Struct;

/**
 *
 */
public class Workload<W extends WorkloadProps> implements Struct {
    W workloadProps;

    public Workload(W workloadProps) {
        this.workloadProps = workloadProps;
    }

    public W workloadProps() {
        return workloadProps;
    }
}
