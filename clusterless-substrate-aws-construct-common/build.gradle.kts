/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    id("clusterless.java-library-conventions")
}

dependencies {
    api(project(":clusterless-common"))
    api(project(":clusterless-model"))
    api(project(":clusterless-substrate-aws-common"))
    api(project(":clusterless-substrate-aws-lambda-arc-model"))

    api("io.clusterless:clusterless-commons-aws")

    // update .github actions when updating this version
    // https://mvnrepository.com/artifact/software.amazon.awscdk/aws-cdk-lib
    val cdkVersion = "2.111.0"
    api("software.amazon.awscdk:aws-cdk-lib:$cdkVersion")
    api("software.amazon.awscdk:glue-alpha:$cdkVersion-alpha.0")
    // https://mvnrepository.com/artifact/software.constructs/constructs
    val constructsVersion = "10.3.0"
    api("software.constructs:constructs:$constructsVersion")
}
