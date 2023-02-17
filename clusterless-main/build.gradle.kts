/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    id("clusterless.java-application-conventions")
}

dependencies {
    implementation(project(":clusterless-common"))
    implementation(project(":clusterless-main-common"))
    implementation(project(":clusterless-model"))
    implementation(project(":clusterless-substrate-aws-kernel"))
}

application {
    // Define the main class for the application.
    applicationName = "cls"
    mainClass.set("clusterless.Main")
}

val awsInstall = tasks.getByPath(":clusterless-substrate-aws-kernel:installDist")

tasks.getAt("installDist").dependsOn(awsInstall)

distributions {
    main {
        contents {
            from(awsInstall) {
                include("bin/")
                include("etc/")
                include("assets/")
            }
        }
    }
}
