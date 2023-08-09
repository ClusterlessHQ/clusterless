/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Resource;
import clusterless.naming.Label;
import clusterless.naming.Ref;
import clusterless.substrate.aws.resources.Refs;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

import java.util.Optional;
import java.util.function.Function;

/**
 *
 */
public class ManagedConstruct extends Construct implements Managed {
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
        Label stage = StagedApp.stagedOf(this).stage();

        return Placement.builder()
                .withAccount(account)
                .withRegion(region)
                .withStage(stage.lowerHyphen())
                .build();
    }

    protected void addArnRefFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        addArnRefFor(ref, construct, value, description);
    }

    protected void addArnRefFor(Ref ref, Construct construct, String value, String description) {
        StagedStack.stagedOf(this)
                .addArnRef(ref, construct, value, description);
    }

    protected void addIdRefFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        addIdRefFor(ref, construct, value, description);
    }

    protected void addIdRefFor(Ref ref, Construct construct, String value, String description) {
        StagedStack.stagedOf(this).addIdRefFor(ref, construct, value, description);
    }

    protected void addNameRefFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        addNameRefFor(ref, construct, value, description);
    }

    protected void addNameRefFor(Ref ref, Construct construct, String value, String description) {
        StagedStack.stagedOf(this)
                .addNameRefFor(ref, construct, value, description);
    }

    protected <T> T resolveArnRef(String ref, Function<String, T> resolver) {
        Construct construct = StagedApp.stagedOf(this).resolveRef(ref);

        if (construct != null) {
            return (T) construct;
        }

        Optional<String> arn = Refs.resolveArn(this, ref);

        return resolver.apply(arn.orElseThrow(() -> new IllegalArgumentException("ref or arn are required" + ref)));
    }
}
