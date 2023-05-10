/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.model.deploy.Resource;
import clusterless.naming.Label;
import clusterless.naming.Ref;
import org.jetbrains.annotations.NotNull;
import software.constructs.Construct;

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

    protected void addArnFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        addArnFor(ref, construct, value, description);
    }

    protected void addArnFor(Ref ref, Construct construct, String value, String description) {
        StagedStack.stagedOf(this).addArnFor(ref, construct, value, description);
    }

    protected void addIdFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        addIdFor(ref, construct, value, description);
    }

    protected void addIdFor(Ref ref, Construct construct, String value, String description) {
        StagedStack.stagedOf(this).addIdFor(ref, construct, value, description);
    }

    protected void addNameFor(Resource resource, Construct construct, String value, String description) {
        Ref ref = Ref.ref()
                .withResourceNs(resource.resourceNs())
                .withResourceType(resource.resourceType())
                .withResourceName(resource.name());

        addNameFor(ref, construct, value, description);
    }

    protected void addNameFor(Ref ref, Construct construct, String value, String description) {
        StagedStack.stagedOf(this).addNameFor(ref, construct, value, description);
    }
}
