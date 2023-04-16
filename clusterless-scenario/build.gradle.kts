/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    id("clusterless.java-application-conventions")
    id("io.github.chklauser.sjsonnet") version "0.1.0-rc.1"
}

dependencies {
    implementation(project(":clusterless-common"))
    implementation(project(":clusterless-main-common"))

    // the log4j exclusion works around the strict version requirement
    val conductor = "3.13.5"
    implementation("com.netflix.conductor:conductor-core:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-common:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-rest:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-client:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-redis-persistence:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-java-sdk:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }

    implementation("javax.validation:validation-api:2.0.1.Final")

    val springBoot = "2.7.3"
    implementation("org.springframework.boot:spring-boot-starter:$springBoot") {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBoot")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBoot")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBoot")
    implementation("org.springframework.retry:spring-retry:2.0.1")

    implementation("org.apache.logging.log4j:log4j-web")

    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:2.3.3")

    implementation("com.google.guava:guava")
}

application {
    applicationName = "cls-scenario"
    mainClass.set("clusterless.scenario.Main")
}

idea {
    module {
        resourceDirs.add(file("src/main/cls"))
    }
}

sjsonnet {
    create("scenarios") {
        indent.set(2)
        sources.from(file("src/main/cls/scenarios"))
        sources.filter { f -> f.extension == "jsonnet" }
        externalVariables.putAll(
            properties.filter { p -> p.key.startsWith("aws.") }.toMap()
        )
    }
}

val copyScenarios = tasks.register<Copy>("copyScenarios") {
    val jsonnet = tasks.named("jsonnetScenariosGenerate")
    dependsOn.add(jsonnet)
    from("src/main/cls/scenarios") {
        exclude("**/*.jsonnet")
    }
    from(jsonnet) {
        exclude("**/scenario.json")
    }
    into(layout.buildDirectory.dir("scenarios"))
}

tasks.named<JavaExec>("run") {
    dependsOn.add(copyScenarios)
    val mainInstall = tasks.getByPath(":clusterless-main:installDist") as Sync

    dependsOn.add(mainInstall)

//    jvmArgs = listOf("-Dlog4j.debug")

    args = listOf(
        "--cls-app",
        "${mainInstall.destinationDir.absolutePath}/bin/cls",
        "-f",
        copyScenarios.get().destinationDir.absolutePath
    )
}
