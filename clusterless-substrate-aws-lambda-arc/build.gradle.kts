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
    implementation(project(":clusterless-substrate-aws-lambda-common"))
    implementation(project(":clusterless-substrate-aws-lambda-arc-model"))

    implementation("software.amazon.awssdk:s3")

    // https://docs.aws.amazon.com/lambda/latest/dg/java-package.html#java-package-libraries
    implementation("com.amazonaws:aws-lambda-java-core")
    implementation("com.amazonaws:aws-lambda-java-log4j2")

    testImplementation(testFixtures(project(":clusterless-substrate-aws-lambda-common")))
}

tasks.register<Zip>("packageAll") {
    from(tasks.compileJava)
    from(tasks.processResources)

    into("lib") {
        from(configurations.runtimeClasspath)
        dirMode = 755
        fileMode = 755
        isReproducibleFileOrder = true
        isPreserveFileTimestamps = false
    }
}

tasks.build {
    dependsOn.add("packageAll")
}
