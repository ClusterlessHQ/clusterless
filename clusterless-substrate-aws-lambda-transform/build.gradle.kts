/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    id("clusterless.java-lambda-conventions")
    id("org.openapi.generator") version "6.6.0"
}

val generatedRoot = layout.buildDirectory.dir("generated/openapi")
val generatedSource = generatedRoot.get().dir("src/main/java")

sourceSets {
    getByName("main") {
        java {
            srcDir(generatedSource)
        }
    }
}

idea {
    module {
        sourceDirs.add(file("src/main/json"))
        generatedSourceDirs.add(generatedSource.asFile)
    }
}

dependencies {
    implementation(project(":clusterless-common"))
    implementation(project(":clusterless-model"))
    implementation(project(":clusterless-substrate-aws-common"))
    implementation(project(":clusterless-substrate-aws-lambda-common"))
    implementation(project(":clusterless-substrate-aws-lambda-transform-model"))

    implementation("software.amazon.awssdk:sqs")
    implementation("software.amazon.awssdk:cloudwatchlogs")

//    implementation("com.cronutils:cron-utils")

    implementation("com.amazonaws:aws-lambda-java-events")
    implementation("com.amazonaws:aws-lambda-java-serialization")

    testImplementation(testFixtures(project(":clusterless-substrate-aws-lambda-common")))
}

val openApiGenerateObjectCreated =
    tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateObjectCreated") {
        generatorName.set("java")
        inputSpec.set(layout.projectDirectory.file("src/main/json/ObjectCreated.json").toString())
        outputDir.set(generatedRoot.get().toString())
        modelPackage.set("clusterless.aws.lambda.transform.json.object")

        configOptions.put("library", "native")
        configOptions.put("serializationLibrary", "jackson")
        configOptions.put("dateLibrary", "java8")
        // AWS adds fields to its event schemas without notice; never fail on one we do not declare
        configOptions.put("additionalModelTypeAnnotations", "@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)")

        globalProperties.put("modelDocs", "false")

        globalProperties.put("apis", "false")
        globalProperties.put("invokers", "false")
        globalProperties.put("models", "")

        generateModelTests.set(false)
        skipValidateSpec.set(false)
//    logToStderr = true
//    generateAliasAsModel = false
        enablePostProcessFile.set(false)
    }
val openApiGenerateScheduledEvent =
    tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateScheduledEvent") {
        generatorName.set("java")
        inputSpec.set(layout.projectDirectory.file("src/main/json/ScheduledJson.json").toString())
        outputDir.set(generatedRoot.get().toString())
        modelPackage.set("clusterless.aws.lambda.transform.json.event")

        configOptions.put("library", "native")
        configOptions.put("serializationLibrary", "jackson")
        configOptions.put("dateLibrary", "java8")
        // AWS adds fields to its event schemas without notice; never fail on one we do not declare
        configOptions.put("additionalModelTypeAnnotations", "@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)")

        globalProperties.put("modelDocs", "false")

        globalProperties.put("apis", "false")
        globalProperties.put("invokers", "false")
        globalProperties.put("models", "")

        generateModelTests.set(false)
        skipValidateSpec.set(false)
//    logToStderr = true
//    generateAliasAsModel = false
        enablePostProcessFile.set(false)
    }

tasks.compileJava {
    dependsOn.add(openApiGenerateObjectCreated)
    dependsOn.add(openApiGenerateScheduledEvent)
}

tasks.register<Zip>("packageAll") {
    from(tasks.compileJava)
    from(tasks.processResources)

    into("lib") {
        from(configurations.runtimeClasspath)
        dirPermissions {
            unix("rwxr-xr-x")
        }
        filePermissions {
            unix("rwxr-xr-x")
        }
        isReproducibleFileOrder = true
        isPreserveFileTimestamps = false
    }
}

tasks.build {
    dependsOn.add("packageAll")
}
