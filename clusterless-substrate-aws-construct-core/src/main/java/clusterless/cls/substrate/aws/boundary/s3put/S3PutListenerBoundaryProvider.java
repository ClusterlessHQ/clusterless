/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.boundary.s3put;

import clusterless.cls.managed.component.BoundaryComponentService;
import clusterless.cls.managed.component.ProvidesComponent;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:s3PutListenerBoundary",
        synopsis = "A boundary that listens for S3 Put events and publishes an availability event for the dataset.",
        description = """
                This boundary listens for put events against the specified bucket and key prefix.
                                
                If events arrive frequently in an interval, change the eventArrival to "frequent", this will
                cause the boundary to emit an availability event after the end of every interval regardless of
                available put events. If no put events are found, the dataset manifest status for the lot interval
                will be set to "empty".
                                
                lotUnit: Fourths|Sixth|Twelfths|etc
                    the interval of a lot, see documentation for supported intervals
                                
                eventArrival: infrequent|frequent
                    expected frequency of event arrivals
                    infrequent is once per interval and frequent more than once per interval
                                
                infrequent.lotSource: objectModifiedTime|eventTime
                    objectModifiedTime will use the last modified time of the object
                    eventTime will use the time the event was received by the listener
                                
                infrequent.enableEventBridge: true|false
                    true will enable event bridge on the bucket
                    if the bucket was not declared with eventBridgeNotification enabled, it must be set here

                frequent.queueFetchWaitSec: seconds
                    The duration (in seconds) for which the call waits for a message to arrive in the queue
                    before returning. If a message is available, the call returns sooner than WaitTimeSeconds.
                    If no messages are available and the wait time expires, the call returns successfully with an
                    empty list of messages.
                    It is recommended to leave this value at zero (0).

                For frequently arriving events, all paths are collected until the end of the interval, except those
                paths that do not pass the filter, if given. The filter is a list of include and exclude patterns.
                                
                Where:
                - ? matches one character
                - * matches zero or more characters
                - ** matches zero or more directories in a path
                                
                A common exclude pattern would be '**/_*'. This would exclude all files that start with an underscore,
                like '_SUCCESS' or '_metadata'.

                frequent.filter.includes: A list of include patterns.
                    Default is '**'.
                frequent.filter.excludes: A list of exclude patterns.
                frequent.filter.pathSeparator: The path separator to use when matching patterns.
                    Default is '/'.
                frequent.filter.ignoreCase: Whether to ignore case when matching patterns.
                    Default is false.
                """
)
public class S3PutListenerBoundaryProvider implements BoundaryComponentService<ManagedComponentContext, S3PutListenerBoundary, S3PutListenerBoundaryConstruct> {
    @Override
    public S3PutListenerBoundaryConstruct create(ManagedComponentContext context, S3PutListenerBoundary boundary) {
        return new S3PutListenerBoundaryConstruct(context, boundary);
    }

    @Override
    public Class<S3PutListenerBoundary> modelClass() {
        return S3PutListenerBoundary.class;
    }
}
