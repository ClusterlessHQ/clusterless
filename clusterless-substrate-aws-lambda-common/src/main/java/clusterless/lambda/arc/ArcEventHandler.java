/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.lambda.EventHandler;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.util.Env;

/**
 *
 */
public abstract class ArcEventHandler extends EventHandler<ArcNotifyEvent, ArcEventContext> {
    protected static final ArcProps arcProps = Env.fromEnv(
            ArcProps.class,
            () -> ArcProps.Builder.builder()
                    .build()
    );

    public ArcEventHandler() {
        super(ArcNotifyEvent.class);
    }

    @Override
    protected ArcEventContext createEventContext() {
        return new ArcEventContext();
    }

}
