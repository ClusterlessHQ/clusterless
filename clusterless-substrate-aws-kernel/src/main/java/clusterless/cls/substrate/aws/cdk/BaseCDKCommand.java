/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk;

import clusterless.cls.model.Loader;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.substrate.aws.AwsConfig;
import clusterless.cls.substrate.aws.CommonCommand;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.store.StateStore;
import clusterless.cls.substrate.store.Stores;
import clusterless.cls.util.ExitCodeException;
import clusterless.commons.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    protected void confirmBootstrapForPlacements(List<File> projectFiles, String awsProfile, boolean dryRun) throws IOException {
        List<Placement> missing = findPlacementsMissingBootstrap(projectFiles, awsProfile, dryRun);

        if (missing.isEmpty()) {
            return;
        }

        Placement placement = missing.get(0);
        String account = placement.account();
        String region = placement.region();
        String stage = placement.stage();
        String message = String.format("must bootstrap account: %s, region: %s, stage: %s, try: %s, or deploy with --bootstrap", account, region, Strings.nullToEmpty(stage), bootstrapCommandFor(placement));
        LOG.error(message);

        throw new IllegalStateException(message);
    }

    /**
     * Returns the placements declared in the given project files whose bootstrap meta bucket does not exist, or
     * an empty list on a dry run.
     */
    protected List<Placement> findPlacementsMissingBootstrap(List<File> projectFiles, String awsProfile, boolean dryRun) throws IOException {
        Set<Placement> placements = new Loader(projectFiles)
                .readObjects(Provider.NAME, Deployable.PROVIDER_POINTER, Deployable.class, Deployable::setSourceFile)
                .stream()
                .map(Deployable::placement)
                .collect(Collectors.toSet());

        if (dryRun) {
            LOG.info("skipping confirming bootstrap, dry run");
            return List.of();
        }

        S3 s3 = createS3(awsProfile);
        List<Placement> missing = new ArrayList<>();

        for (Placement placement : placements) {
            String bucketName = Stores.bootstrapStoreName(StateStore.Meta, placement);

            LOG.info("confirming bootstrap: {}", bucketName);

            S3.Response response = s3.exists(placement.region(), bucketName);

            if (s3.exists(response)) {
                continue;
            }

            // only a missing bucket means not bootstrapped, access denied or missing credentials must not be
            // reported as "must bootstrap", nor trigger a bootstrap
            if (!isMissingBucket(response)) {
                String message = String.format("unable to confirm bootstrap bucket: %s, %s", bucketName, s3.error(response));
                LOG.error(message);
                throw new ExitCodeException(message, response.exception(), 1);
            }

            LOG.info("bootstrap bucket does not exist: {}", bucketName);
            missing.add(placement);
        }

        return missing;
    }

    /**
     * Creates the S3 client wrapper used to confirm bootstrap buckets, a test seam.
     */
    protected S3 createS3(String awsProfile) {
        return new S3(awsProfile);
    }

    protected static boolean isMissingBucket(S3.Response response) {
        return response.statusCode() == 404;
    }

    protected static String bootstrapCommandFor(Placement placement) {
        String command = String.format("cls bootstrap --account %s --region %s", placement.account(), placement.region());

        if (Strings.emptyToNull(placement.stage()) == null) {
            return command;
        }

        return String.format("%s --stage %s", command, placement.stage());
    }
}
