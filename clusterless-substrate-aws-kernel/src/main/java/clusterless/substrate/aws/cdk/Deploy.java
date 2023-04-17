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
import clusterless.model.deploy.Placement;
import clusterless.startup.Loader;
import clusterless.substrate.aws.CommonCommand;
import clusterless.substrate.aws.ProcessExec;
import clusterless.substrate.aws.sdk.S3;
import clusterless.substrate.aws.store.StateStore;
import clusterless.substrate.aws.store.Stores;
import clusterless.util.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "deploy"
)
public class Deploy extends CommonCommand implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Deploy.class);
    @CommandLine.Mixin
    DeployCommandOptions commandOptions = new DeployCommandOptions();
    @CommandLine.Mixin
    ProcessExec processExec = new ProcessExec(commandOptions);

    @Override
    public Integer call() throws Exception {

        Set<Placement> placements = new Loader(commandOptions.projectFiles())
                .readObjects(CDK.PROVIDER, Deployable.PROVIDER_POINTER, Deployable.class, Deployable::setSourceFile)
                .stream()
                .map(Deployable::placement)
                .collect(Collectors.toSet());

        S3 s3 = new S3(processExec.profile());

        for (Placement placement : placements) {
            String bucketName = Stores.bootstrapStoreName(StateStore.Meta, placement);

            LOG.info("confirming bootstrap: {}", bucketName);

            S3.Response response = s3.exists(placement.region(), bucketName);

            if (s3.exists(response)) {
                continue;
            }

            // todo: add copy/paste bootstrap command here
            String account = placement.account();
            String region = placement.region();
            String stage = placement.stage();
            LOG.error("bootstrap bucket does not exist: {}, {}", bucketName, s3.error(response));
            String message = String.format("must bootstrap account: %s, region: %s, stage: %s", account, region, Strings.nullToEmpty(stage));
            LOG.error(message);

            throw new IllegalStateException(message);
        }

        return processExec.executeLifecycleProcess(
                getCommonConfig(),
                getProviderConfig(),
                commandOptions,
                "deploy",
                getRequireDeployApproval()
        );
    }
}
