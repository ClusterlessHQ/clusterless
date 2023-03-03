/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.workload.s3copy;

import clusterless.lambda.arc.ArcEventContext;
import clusterless.lambda.arc.ArcEventHandler;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import com.amazonaws.services.lambda.runtime.Context;

/**
 *
 */
public class S3CopyArcEventHandler extends ArcEventHandler {
    @Override
    protected void handleEvent(ArcNotifyEvent event, Context context, ArcEventContext eventContext) {
        // get manifests

        //  copy files

        // write manifest
    }
}
