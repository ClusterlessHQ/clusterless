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
    api(project(":clusterless-substrate-aws-lambda-arc"))

    // https://mvnrepository.com/artifact/software.amazon.awscdk/aws-cdk-lib
    api("software.amazon.awscdk:aws-cdk-lib:2.72.1")
    // https://mvnrepository.com/artifact/software.constructs/constructs
    api("software.constructs:constructs:10.1.299")
}
