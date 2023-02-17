/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import clusterless.command.DeployCommandOptions;
import clusterless.model.deploy.Deployable;
import clusterless.startup.Loader;
import clusterless.substrate.aws.ProcessExec;
import clusterless.substrate.aws.resources.Buckets;
import clusterless.substrate.aws.sdk.S3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "deploy"
)
public class Deploy implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Deploy.class);
    @CommandLine.Mixin
    ProcessExec processExec = new ProcessExec();
    @CommandLine.Mixin
    DeployCommandOptions commandOptions = new DeployCommandOptions();

    @Override
    public Integer call() throws Exception {

        Set<Deployable.Placement> placements = new Loader(commandOptions.projectFiles())
                .readObjects(CDK.PROVIDER, Deployable.PROVIDER_POINTER, Deployable.class, Deployable::setSourceFile)
                .stream()
                .map(Deployable::placement)
                .collect(Collectors.toSet());

        S3 s3 = new S3(processExec.profile());

        for (Deployable.Placement placement : placements) {
            String account = placement.account();
            String region = placement.region();
            String stage = placement.stage();
            String bucketName = Buckets.bootstrapBucketName(Buckets.BootstrapBucket.METADATA, account, region, stage);

            LOG.info("confirming bootstrap: {}", bucketName);

            S3.Response response = s3.exists(placement.region(), bucketName);

            if (s3.exists(response)) {
                continue;
            }

            // todo: add copy/paste bootstrap command here
            LOG.error("bootstrap bucket does not exist: {}, {}", bucketName, s3.error(response));
            String message = "must bootstrap account: %s, region: %s, stage: %s".formatted(account, region, stage);
            LOG.error(message);

            throw new IllegalStateException(message);
        }

        return processExec.executeLifecycleProcess("deploy", commandOptions);
    }
}
