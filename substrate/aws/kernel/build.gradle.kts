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
    implementation(project(":substrate:aws:construct:common"))
    implementation(project(":substrate:aws:construct:boundary"))
    implementation(project(":substrate:aws:construct:arc"))
}

application {
    applicationName = "awsKernel"
//    mainModule.set("clusterless.substrate.aws.kernel")
    mainClass.set("clusterless.substrate.aws.Kernel")
}
