/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

pluginManagement {
    // Include 'plugins build' to define convention plugins.
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

rootProject.name = "clusterless"

include("clusterless-common")
include("clusterless-model")
include("clusterless-main-common")
include("clusterless-main")

// AWS Substrate
// shared aws sdk functionality
include("clusterless-substrate-aws-common")

include("clusterless-substrate-aws-kernel")

// shared aws cdk functionality
include("clusterless-substrate-aws-construct-common")
// core provided deploy constructs
include("clusterless-substrate-aws-construct-core")

// common lambda apis
include("clusterless-substrate-aws-lambda-common")

// lambdas for arc management
include("clusterless-substrate-aws-lambda-arc")

// service lambdas
include("clusterless-substrate-aws-service-transform")

// core lambda workloads
include("clusterless-substrate-aws-lambda-workload")

// test scenarios
include("clusterless-scenario")
