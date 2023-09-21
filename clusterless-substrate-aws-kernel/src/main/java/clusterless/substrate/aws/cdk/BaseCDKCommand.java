/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import clusterless.model.Loader;
import clusterless.model.deploy.Deployable;
import clusterless.model.deploy.Placement;
import clusterless.substrate.aws.AwsConfig;
import clusterless.substrate.aws.CommonCommand;
import clusterless.substrate.aws.sdk.S3;
import clusterless.substrate.store.StateStore;
import clusterless.substrate.store.Stores;
import clusterless.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class BaseCDKCommand extends CommonCommand {
    private static final Logger LOG = LoggerFactory.getLogger(BaseCDKCommand.class);

    protected List<String> getRequireDeployApproval(Boolean approve) {
        AwsConfig.CDK.Approval approval = getProviderConfig().cdk().requireDeployApproval();

        if (approve != null) {
            approval = approve ? AwsConfig.CDK.Approval.never : AwsConfig.CDK.Approval.broadening;
        }

        return List.of(
                "--require-approval", approval.value()
        );
    }

    @NotNull
    protected List<String> getRequireDestroyApproval(Boolean approve) {
        if ((approve != null && approve) || !getProviderConfig().cdk().requireDestroyApproval()) {
            return List.of(
                    "--force"
            );
        }

        return Collections.emptyList();
    }

    protected void confirmBootstrapForPlacements(List<File> projectFiles, String awsProfile) throws IOException {
        Set<Placement> placements = new Loader(projectFiles)
                .readObjects(Provider.NAME, Deployable.PROVIDER_POINTER, Deployable.class, Deployable::setSourceFile)
                .stream()
                .map(Deployable::placement)
                .collect(Collectors.toSet());

        S3 s3 = new S3(awsProfile);

        for (Placement placement : placements) {
            String bucketName = Stores.bootstrapStoreName(StateStore.Meta, placement);

            LOG.info("confirming bootstrap: {}", bucketName);

            S3.Response response = s3.exists(placement.region(), bucketName);

            if (s3.exists(response)) {
                continue;
            }

            // TODO: add copy/paste bootstrap command here
            String account = placement.account();
            String region = placement.region();
            String stage = placement.stage();
            LOG.error("bootstrap bucket does not exist: {}, {}", bucketName, s3.error(response));
            String message = String.format("must bootstrap account: %s, region: %s, stage: %s", account, region, Strings.nullToEmpty(stage));
            LOG.error(message);

            throw new IllegalStateException(message);
        }
    }
}
