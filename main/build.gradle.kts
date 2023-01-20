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
    implementation(project(":model"))
    implementation(project(":substrate:aws:kernel"))
}

application {
    // Define the main class for the application.
    applicationName = "cls"
    mainClass.set("clusterless.Main")
}
