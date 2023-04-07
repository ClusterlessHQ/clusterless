/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.lambda.EventResultHandler;
import clusterless.model.deploy.WorkloadProps;
import clusterless.substrate.aws.event.ArcStateContext;
import clusterless.util.Env;

import java.net.URI;
import java.util.Map;

/**
 *
 */
public abstract class ArcEventHandler<P extends WorkloadProps> extends EventResultHandler<ArcStateContext, Map<String, URI>, ArcEventObserver> {
    protected static final ArcProps<?> arcProps = Env.fromEnv(
            ArcProps.class,
            () -> ArcProps.builder().build()
    );

    @SuppressWarnings("unchecked")
    protected P workloadProperties() {
        return (P) arcProps.workloadProps();
    }

    public ArcEventHandler() {
        super(ArcStateContext.class, getMapTypeFor(String.class, URI.class));

        logObject("using arcProps", arcProps);
    }
}
