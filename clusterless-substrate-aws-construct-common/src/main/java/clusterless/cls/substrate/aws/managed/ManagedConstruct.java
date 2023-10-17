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
import clusterless.cls.naming.Label;
import clusterless.cls.naming.Ref;
import clusterless.cls.substrate.aws.scoped.ScopedApp;
import clusterless.cls.substrate.aws.scoped.ScopedConstruct;
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
        Label stage = ScopedApp.stagedOf(this).stage();

        return Placement.builder()
                .withAccount(account)
                .withRegion(region)
                .withStage(stage.lowerHyphen())
                .build();
    }

    protected void addIdRefFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        addIdRefFor(ref, construct, value, description);
    }

    protected void addArnRefFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        addArnRefFor(ref, construct, value, description);
    }

    protected void addNameRefFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        addNameRefFor(ref, construct, value, description);
    }


}
