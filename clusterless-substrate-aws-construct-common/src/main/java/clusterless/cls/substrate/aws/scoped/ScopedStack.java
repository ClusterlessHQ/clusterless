/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.scoped;

import clusterless.cls.naming.Ref;
import clusterless.cls.naming.Stage;
import clusterless.cls.substrate.aws.construct.OutputConstruct;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Token;
import software.constructs.Construct;

/**
 *
 */
public class ScopedStack extends Stack {
    public static ScopedStack scopedOf(Construct construct) {
        Stack stack = Stack.of(construct);
        if (stack instanceof ScopedStack) {
            return (ScopedStack) stack;
        }

        throw new IllegalArgumentException("construct does not belong to a ScopedStack, found: " + stack.getClass().getName());
    }

    private final Stage stage;

    public ScopedStack(@NotNull ScopedApp app, @Nullable String id, @Nullable StackProps props) {
        super(app, id, props);
        stage = app.stage();
    }

    public ScopedStack(@NotNull ScopedApp app, @Nullable String id) {
        super(app, id);
        stage = app.stage();
    }

    public ScopedStack(@NotNull ScopedApp app) {
        super(app);
        stage = app.stage();
    }

    public Stage stage() {
        return stage;
    }

    protected void addNameRefFor(Ref ref, String value, String description) {
        addNameRefFor(ref, null, value, description);
    }

    public void addNameRefFor(Ref ref, Construct construct, String value, String description) {
        Ref.Qualifier qualifier = Ref.Qualifier.Name;
        Ref qualifiedRef = withContext(ref).withQualifier(qualifier);

        OutputConstruct outputConstruct = new OutputConstruct(this, qualifiedRef, value, description);

        if (!Token.isUnresolved(value)) {
            ScopedApp.stagedOf(this)
                    .stagedMeta()
                    .setName(ref.resourceType().value(), value);
        }

        ScopedApp.stagedOf(this)
                .stagedMeta()
                .setNameRef(ref.resourceType().value(), outputConstruct.exportName());

        if (construct != null) {
            ScopedApp.stagedOf(this)
                    .addRef(qualifiedRef, construct);
        }
    }

    protected void addIdRefFor(Ref ref, String value, String description) {
        addIdRefFor(ref, null, value, description);
    }

    public void addIdRefFor(Ref ref, Construct construct, String value, String description) {
        Ref.Qualifier qualifier = Ref.Qualifier.Id;
        Ref qualifiedRef = withContext(ref).withQualifier(qualifier);

        OutputConstruct outputConstruct = new OutputConstruct(this, qualifiedRef, value, description);

        if (!Token.isUnresolved(value)) {
            ScopedApp.stagedOf(this)
                    .stagedMeta()
                    .setId(ref.resourceType().value(), value);
        }

        ScopedApp.stagedOf(this)
                .stagedMeta()
                .setIdRef(ref.resourceType().value(), outputConstruct.exportName());

        if (construct != null) {
            ScopedApp.stagedOf(this)
                    .addRef(qualifiedRef, construct);
        }
    }

    protected void addArnRef(Ref ref, String value, String description) {
        addArnRef(ref, null, value, description);
    }

    public void addArnRef(Ref ref, Construct construct, String value, String description) {
        Ref.Qualifier qualifier = Ref.Qualifier.Arn;
        Ref qualifiedRef = withContext(ref).withQualifier(qualifier);

        OutputConstruct outputConstruct = new OutputConstruct(this, qualifiedRef, value, description);

        ScopedApp.stagedOf(this)
                .stagedMeta()
                .setArnRef(ref.resourceType().value(), outputConstruct.exportName());

        if (construct != null) {
            ScopedApp.stagedOf(this)
                    .addRef(qualifiedRef, construct);
        }
    }

    protected Ref withContext(Ref ref) {
        return ref
                .withProvider("aws")
                .withStage(stage());
    }
}
