/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("clusterless.java-common-conventions")

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}
