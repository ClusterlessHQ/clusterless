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

    protected void addNameFor(ExportRef ref, String value, String description) {
        ExportRef.ExportQualifier qualifier = ExportRef.ExportQualifier.Name;
        Label uniqueName = withContext(ref).withQualifier(qualifier).label();

        OutputConstruct outputConstruct = new OutputConstruct(this, uniqueName, value, description);

        if (!Token.isUnresolved(value)) {
            StagedApp.stagedOf(this)
                    .deployMeta()
                    .setName(ref.resourceType().value(), value);
        }

        StagedApp.stagedOf(this)
                .deployMeta()
                .setNameRef(ref.resourceType().value(), outputConstruct.exportName());

    }

    protected void addIdFor(ExportRef ref, String value, String description) {
        ExportRef.ExportQualifier qualifier = ExportRef.ExportQualifier.Id;
        Label uniqueName = withContext(ref).withQualifier(qualifier).label();

        OutputConstruct outputConstruct = new OutputConstruct(this, uniqueName, value, description);

        if (!Token.isUnresolved(value)) {
            StagedApp.stagedOf(this)
                    .deployMeta()
                    .setId(ref.resourceType().value(), value);
        }

        StagedApp.stagedOf(this)
                .deployMeta()
                .setIdRef(ref.resourceType().value(), outputConstruct.exportName());

    }

    protected void addArnFor(ExportRef ref, String value, String description) {
        ExportRef.ExportQualifier qualifier = ExportRef.ExportQualifier.Arn;
        Label uniqueName = withContext(ref).withQualifier(qualifier).label();

        OutputConstruct outputConstruct = new OutputConstruct(this, uniqueName, value, description);

        StagedApp.stagedOf(this)
                .deployMeta()
                .setArnRef(ref.resourceType().value(), outputConstruct.exportName());
    }

    protected ExportRef withContext(ExportRef ref) {
        return ref
                .withProvider("aws")
                .withStage(stage());
    }
}
