/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.naming.Ref;
import clusterless.naming.Stage;
import clusterless.substrate.aws.construct.OutputConstruct;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Token;
import software.constructs.Construct;

/**
 *
 */
public class StagedStack extends Stack {
    public static StagedStack stagedOf(Construct construct) {
        return (StagedStack) Stack.of(construct);
    }

    private final Stage stage;

    public StagedStack(@NotNull StagedApp app, @Nullable String id, @Nullable StackProps props) {
        super(app, id, props);

        stage = app.stage();
    }

    public StagedStack(@NotNull StagedApp app, @Nullable String id) {
        super(app, id);

        stage = app.stage();
    }

    public StagedStack(@NotNull StagedApp app) {
        super(app);

        stage = app.stage();
    }

    public Stage stage() {
        return stage;
    }

    protected void addNameFor(Ref ref, String value, String description) {
        addNameFor(ref, null, value, description);
    }

    protected void addNameFor(Ref ref, Construct construct, String value, String description) {
        Ref.Qualifier qualifier = Ref.Qualifier.Name;
        Ref qualifiedRef = withContext(ref).withQualifier(qualifier);

        OutputConstruct outputConstruct = new OutputConstruct(this, qualifiedRef, value, description);

        if (!Token.isUnresolved(value)) {
            StagedApp.stagedOf(this)
                    .deployMeta()
                    .setName(ref.resourceType().value(), value);
        }

        StagedApp.stagedOf(this)
                .deployMeta()
                .setNameRef(ref.resourceType().value(), outputConstruct.exportName());

        if (construct != null) {
            StagedApp.stagedOf(this)
                    .addRef(qualifiedRef, construct);
        }
    }

    protected void addIdFor(Ref ref, String value, String description) {
        addIdFor(ref, null, value, description);
    }

    protected void addIdFor(Ref ref, Construct construct, String value, String description) {
        Ref.Qualifier qualifier = Ref.Qualifier.Id;
        Ref qualifiedRef = withContext(ref).withQualifier(qualifier);

        OutputConstruct outputConstruct = new OutputConstruct(this, qualifiedRef, value, description);

        if (!Token.isUnresolved(value)) {
            StagedApp.stagedOf(this)
                    .deployMeta()
                    .setId(ref.resourceType().value(), value);
        }

        StagedApp.stagedOf(this)
                .deployMeta()
                .setIdRef(ref.resourceType().value(), outputConstruct.exportName());

        if (construct != null) {
            StagedApp.stagedOf(this)
                    .addRef(qualifiedRef, construct);
        }
    }

    protected void addArnFor(Ref ref, String value, String description) {
        addArnFor(ref, null, value, description);
    }

    protected void addArnFor(Ref ref, Construct construct, String value, String description) {
        Ref.Qualifier qualifier = Ref.Qualifier.Arn;
        Ref qualifiedRef = withContext(ref).withQualifier(qualifier);

        OutputConstruct outputConstruct = new OutputConstruct(this, qualifiedRef, value, description);

        StagedApp.stagedOf(this)
                .deployMeta()
                .setArnRef(ref.resourceType().value(), outputConstruct.exportName());

        if (construct != null) {
            StagedApp.stagedOf(this)
                    .addRef(qualifiedRef, construct);
        }
    }

    protected Ref withContext(Ref ref) {
        return ref
                .withProvider("aws")
                .withStage(stage());
    }
}
