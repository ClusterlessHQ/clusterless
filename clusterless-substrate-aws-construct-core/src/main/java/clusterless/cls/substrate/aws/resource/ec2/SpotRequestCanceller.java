/*
 * Copyright (c) 2023-2026 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.ec2;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.ArnComponents;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.customresources.Provider;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * The custom resource provider, one per stack, that cancels an ec2Instance's persistent Spot request
 * when the instance leaves the stack, see spot-request-canceller.js.
 * <p>
 * Lifecycle glue for one resource, calling only the lambda runtime's aws sdk, so it is inline here
 * rather than packaged with the clusterless lambdas.
 */
class SpotRequestCanceller extends Construct {
    static final String ID = "SpotRequestCanceller";
    static final String RESOURCE_TYPE = "Custom::SpotInstanceRequest";
    static final String HANDLER = "spot-request-canceller.js";

    private final Provider provider;

    static String serviceToken(@NotNull Construct scope) {
        Stack stack = Stack.of(scope);
        SpotRequestCanceller canceller = (SpotRequestCanceller) stack.getNode().tryFindChild(ID);

        if (canceller == null) {
            canceller = new SpotRequestCanceller(stack);
        }

        return canceller.provider.getServiceToken();
    }

    private SpotRequestCanceller(@NotNull Stack stack) {
        super(stack, ID);

        Function handler = Function.Builder.create(this, "Handler")
                .runtime(Runtime.NODEJS_24_X)
                .handler("index.handler")
                .code(Code.fromInline(source()))
                .timeout(Duration.minutes(1))
                .build();

        // neither describe call can be scoped to a resource
        handler.addToRolePolicy(PolicyStatement.Builder.create()
                .actions(List.of("ec2:DescribeInstances", "ec2:DescribeSpotInstanceRequests"))
                .resources(List.of("*"))
                .build());

        handler.addToRolePolicy(PolicyStatement.Builder.create()
                .actions(List.of("ec2:CancelSpotInstanceRequests"))
                .resources(List.of(arn(stack, "spot-instances-request")))
                .build());

        // the instances a cancelled request launched outside the stack
        handler.addToRolePolicy(PolicyStatement.Builder.create()
                .actions(List.of("ec2:TerminateInstances"))
                .resources(List.of(arn(stack, "instance")))
                .build());

        provider = Provider.Builder.create(this, "Provider")
                .onEventHandler(handler)
                .build();
    }

    private static String arn(Stack stack, String resource) {
        return stack.formatArn(ArnComponents.builder()
                .service("ec2")
                .resource(resource)
                .resourceName("*")
                .build());
    }

    static String source() {
        try (InputStream stream = Objects.requireNonNull(SpotRequestCanceller.class.getResourceAsStream(HANDLER), HANDLER)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
