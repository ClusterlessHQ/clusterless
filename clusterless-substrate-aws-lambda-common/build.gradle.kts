/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    id("clusterless.java-library-conventions")
    id("clusterless.java-override-conventions-jdk11")
}

dependencies {
    implementation(project(":clusterless-common"))
    implementation(project(":clusterless-model"))
    implementation(project(":clusterless-substrate-aws-common"))

    implementation("com.google.guava:guava")

    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    api("software.amazon.awssdk:s3")
    api("software.amazon.awssdk:cloudwatch")
    api("software.amazon.awssdk:eventbridge")

    api("com.amazonaws:aws-lambda-java-core")
    api("com.amazonaws:aws-lambda-java-log4j2")

//    implementation("io.github.resilience4j:resilience4j-retry")
}