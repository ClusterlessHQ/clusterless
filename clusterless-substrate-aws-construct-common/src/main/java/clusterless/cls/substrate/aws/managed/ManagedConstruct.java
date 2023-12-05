/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.managed;

import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Resource;
import clusterless.commons.naming.Label;
import clusterless.commons.naming.Ref;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedApp;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedConstruct;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

/**
 *
 */
public class ManagedConstruct extends ScopedConstruct implements Managed {
    private final ManagedComponentContext context;

    public ManagedConstruct(@NotNull ManagedComponentContext context, Label baseId) {
        super(context.parentConstruct(), baseId.camelCase());
        this.context = context;
    }

    public ManagedComponentContext context() {
        return context;
    }

    @NotNull
    protected Placement placement() {
        String account = Stack.of(this).getAccount();
        String region = Stack.of(this).getRegion();
        Label stage = ScopedApp.scopedOf(this).stage();

        return Placement.builder()
                .withAccount(account)
                .withRegion(region)
                .withStage(stage.lowerHyphen())
                .build();
    }

    protected void exportIdRefFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        exportIdRefFor(ref, construct, value, description);
    }

    protected void exportArnRefFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        exportArnRefFor(ref, construct, value, description);
    }

    protected void exportNameRefFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        exportNameRefFor(ref, construct, value, description);
    }
}
