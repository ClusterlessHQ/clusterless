/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.batch;

import clusterless.managed.component.ArcComponentService;
import clusterless.managed.component.ArcLocalExecutor;
import clusterless.managed.component.ProvidesComponent;
import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Workload;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent("aws:core:batchExecArc")
public class BatchExecArcProvider implements ArcComponentService<ManagedComponentContext, BatchExecArc, BatchExecArcConstruct> {

    @Override
    public BatchExecArcConstruct create(ManagedComponentContext context, BatchExecArc model) {
        return new BatchExecArcConstruct(context, model);
    }

    @Override
    public ArcLocalExecutor executor(Placement placement, Arc<? extends Workload<?>> arc) {
        return new BatchExecArcLocalExecutor(placement, (BatchExecArc) arc);
    }

    @Override
    public Class<BatchExecArc> modelClass() {
        return BatchExecArc.class;
    }
}
