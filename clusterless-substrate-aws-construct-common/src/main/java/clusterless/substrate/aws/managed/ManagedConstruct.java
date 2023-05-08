/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.naming.ExportRef;
import clusterless.naming.Label;
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

    protected void addArnFor(String resourceType, String resourceName, String value, String description) {
        ExportRef ref = ExportRef.ref()
                .withResourceType(resourceType)
                .withResourceName(resourceName);

        addArnFor(ref, value, description);
    }

    protected void addArnFor(ExportRef ref, String value, String description) {
        StagedStack.stagedOf(this).addArnFor(ref, value, description);
    }

    protected void addIdFor(String resourceType, String resourceName, String value, String description) {
        ExportRef ref = ExportRef.ref()
                .withResourceType(resourceType)
                .withResourceName(resourceName);

        addIdFor(ref, value, description);
    }

    protected void addIdFor(ExportRef ref, String value, String description) {
        StagedStack.stagedOf(this).addIdFor(ref, value, description);
    }

    protected void addNameFor(String resourceType, String resourceName, String value, String description) {
        ExportRef ref = ExportRef.ref()
                .withResourceType(resourceType)
                .withResourceName(resourceName);

        addNameFor(ref, value, description);
    }

    protected void addNameFor(ExportRef ref, String value, String description) {
        StagedStack.stagedOf(this).addNameFor(ref, value, description);
    }
}
