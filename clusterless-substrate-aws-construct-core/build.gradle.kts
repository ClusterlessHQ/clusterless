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
    implementation(project(":clusterless-common"))
    implementation(project(":clusterless-model"))
    implementation(project(":clusterless-substrate-aws-construct-common"))

    // this is the lambda code added as a convenience
    // final packaging will likely depend on a shared library of structs
    // and a meta-data file referencing the deployable asset
    implementation(project(":clusterless-substrate-aws-service-transform"))
}
