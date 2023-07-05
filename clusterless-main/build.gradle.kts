/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import org.jreleaser.model.Active
import org.jreleaser.model.Distribution
import org.jreleaser.model.Stereotype

plugins {
    id("clusterless.java-application-conventions")
    id("org.jreleaser") version "1.7.0"
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

val awsKernelInstall = tasks.getByPath(":clusterless-substrate-aws-kernel:installDist")

tasks.getAt("installDist").dependsOn(awsKernelInstall)

distributions {
    main {
        distributionBaseName.set("clusterless")
        contents {
            from(awsKernelInstall) {
                include("bin/")
                include("etc/")
                include("assets/")
            }
        }
    }
}

jreleaser {
    project {
        description.set("Clusterless is a framework for building serverless data oriented applications.")
        authors.add("Chris K Wensel")
        copyright.set("Chris K Wensel")
        license.set("MPL-2.0")
        stereotype.set(Stereotype.CLI)
        links {
            homepage.set("https://github.com/ClusterlessHQ")
            documentation.set("https://docs.clusterless.io/")
        }
        inceptionYear.set("2023")
        gitRootSearch.set(true)
    }

    signing {
        armored.set(true)
        active.set(Active.ALWAYS)
        verify.set(false)
    }

    release {
        github {
            overwrite.set(true)
            sign.set(false)
            repoOwner.set("ClusterlessHQ")
            name.set("clusterless")
            username.set("cwensel")
            branch.set("wip-1.0")
            changelog.enabled.set(false)
            milestone.close.set(false)
        }
    }

    distributions {
        create("clusterless") {
            distributionType.set(Distribution.DistributionType.JAVA_BINARY)
            executable {
                name.set("cls")
            }
            artifact {
                path.set(file("build/distributions/{{distributionName}}-{{projectVersion}}.zip"))
            }
        }
    }
}

tasks.register("release") {
    dependsOn("distZip")
    dependsOn("jreleaserRelease")
}
