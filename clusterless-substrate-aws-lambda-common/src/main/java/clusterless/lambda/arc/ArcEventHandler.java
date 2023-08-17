/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.lambda.EventResultHandler;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.deploy.WorkloadProps;
import clusterless.model.manifest.Manifest;
import clusterless.substrate.aws.event.ArcWorkloadContext;
import clusterless.util.Env;
import clusterless.util.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

/**
 *
 */
public abstract class ArcEventHandler<P extends WorkloadProps> extends EventResultHandler<ArcWorkloadContext, Map<String, URI>, ArcEventObserver> {
    private static final Logger LOG = LoggerFactory.getLogger(ArcEventHandler.class);
    /**
     * Not static for tests
     */
    @SuppressWarnings("unchecked")
    private final Lazy<ArcProps<?>> arcProps = Lazy.of(() -> Env.fromEnv(ArcProps.class));

    @SuppressWarnings("unchecked")
    protected ArcProps<P> arcProps() {
        return (ArcProps<P>) arcProps.get();
    }

    @SuppressWarnings("unchecked")
    protected P workloadProperties() {
        return (P) arcProps.get().workloadProps();
    }

    public ArcEventHandler() {
        super(ArcWorkloadContext.class, getMapTypeFor(String.class, URI.class));

        logInfoObject("using arcProps: {}", arcProps.get());
    }

    protected ArcEventObserver observer() {
        return new ArcEventObserver() {
            @Override
            public void applyFromManifest(Manifest manifest) {
                String name = manifest.dataset() != null ? manifest.dataset().name() : null;
                String version = manifest.dataset() != null ? manifest.dataset().version() : null;
                String lotId = manifest.lotId();
                int size = manifest.uris() != null ? manifest.uris().size() : 0;
                LOG.info("manifest from dataset name: {}, version: {}, lot: {}, size: {}", name, version, lotId, size);
            }

            @Override
            public void applyToDataset(String role, SinkDataset sinkDataset) {
                String name = sinkDataset.name();
                String version = sinkDataset.version();
                URI pathURI = sinkDataset.pathURI();
                LOG.info("writing to dataset name: {}, version: {}, with role: {} at {}", name, version, role, pathURI);
            }

            @Override
            public void applyToManifest(String role, URI manifest) {
                LOG.info("write manifest: {}, with role: {}", manifest, role);
            }
        };
    }
}
