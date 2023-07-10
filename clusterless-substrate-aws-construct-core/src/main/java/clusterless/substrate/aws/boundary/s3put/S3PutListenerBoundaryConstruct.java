/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.substrate.aws.construct.IngressBoundaryConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class S3PutListenerBoundaryConstruct extends IngressBoundaryConstruct<S3PutListenerBoundary> {
    private static final Logger LOG = LoggerFactory.getLogger(S3PutListenerBoundaryConstruct.class);
    public S3PutListenerBoundaryConstruct(@NotNull ManagedComponentContext context, @NotNull S3PutListenerBoundary model) {
        super(context, model);

        switch (model().eventArrival()) {
            case infrequent:
                LOG.info("creating infrequent s3 put listener boundary");
                new InfrequentS3PutStrategyBoundaryConstruct(context, model);
                break;
            case frequent:
                LOG.info("creating frequent s3 put listener boundary");
                new FrequentS3PutStrategyBoundaryConstruct(context, model);
                break;
            case veryFrequent:
                throw new UnsupportedOperationException("veryFrequent not yet supported");
        }
    }
}
