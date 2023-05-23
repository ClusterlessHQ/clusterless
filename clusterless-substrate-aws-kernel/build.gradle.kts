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
    implementation(project(":clusterless-main-common"))
    implementation(project(":clusterless-substrate-aws-common"))
    implementation(project(":clusterless-substrate-aws-construct-common"))
    implementation(project(":clusterless-substrate-aws-construct-core"))

    implementation("com.google.guava:guava")
}

application {
    applicationName = "cls-aws"
//    mainModule.set("clusterless.substrate.aws.kernel")
    mainClass.set("clusterless.substrate.aws.Kernel")
}

distributions {
    main {
        contents {
            from(file("src/main/cdk/")) {
                into("etc")
            }
            val serviceTransform: Zip =
                tasks.findByPath(":clusterless-substrate-aws-lambda-transform:packageAll") as Zip
            from(serviceTransform.archiveFile) {
                into("assets")
            }
            val lambdaArc: Zip =
                tasks.findByPath(":clusterless-substrate-aws-lambda-arc:packageAll") as Zip
            from(lambdaArc.archiveFile) {
                into("assets")
            }
            val lambdaWorkload: Zip =
                tasks.findByPath(":clusterless-substrate-aws-lambda-workload:packageAll") as Zip
            from(lambdaWorkload.archiveFile) {
                into("assets")
            }
        }
    }
}

idea {
    module {
        resourceDirs.add(file("src/main/cdk"))
    }
}
