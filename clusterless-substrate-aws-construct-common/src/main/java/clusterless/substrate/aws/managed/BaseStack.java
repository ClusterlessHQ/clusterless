/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.lambda.CfnFunction;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;
import software.constructs.IConstruct;

/**
 *
 */
public class BaseStack extends Stack {
    private static final Logger LOG = LogManager.getLogger(BaseStack.class);

    class LogGroupAspect implements IAspect {

        @Override
        public void visit(@NotNull IConstruct node) {
            System.out.println("node.getClass().getName() = " + node.getClass().getName());

            if (CfnResource.isCfnResource(node)) {
                CfnResource resource = (CfnResource) node;
                System.out.println("resource.getCfnResourceType() = " + resource.getCfnResourceType());
                System.out.println("resource.getNode().getDefaultChild() = " + resource.getNode().getDefaultChild());
            }

            if (node instanceof CfnFunction) {
                CfnFunction function = (CfnFunction) node;
                RetentionDays retentionDays = RetentionDays.ONE_DAY;
                RemovalPolicy removalPolicy = RemovalPolicy.DESTROY;
                String nodeId = function.getNode().getId();
                LOG.info("adding log group for: {}, with retention: {}, policy: {}", nodeId, retentionDays, removalPolicy);


                LogGroup.Builder.create(Stack.of(function), "LogGroup-" + nodeId)
                        .logGroupName("/aws/lambda/" + function.getFunctionName())
                        .removalPolicy(removalPolicy)
                        .retention(retentionDays)
                        .build();
            }
        }
    }

    public BaseStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

//        Aspects.of(this).add(new LogGroupAspect());
    }

    public BaseStack(@Nullable Construct scope, @Nullable String id) {
        super(scope, id);

//        Aspects.of(this).add(new LogGroupAspect());
    }

    public BaseStack(@Nullable Construct scope) {
        super(scope);

//        Aspects.of(this).add(new LogGroupAspect());
    }
}
