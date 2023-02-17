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
    id("org.openapi.generator") version "6.3.0"
}

val generatedRoot = "$buildDir/generated/openapi"
val generatedSource = "$generatedRoot/src/main/java"

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
        generatedSourceDirs.add(file(generatedSource))
    }
}

dependencies {
    implementation(project(":clusterless-common"))
    implementation(project(":clusterless-model"))
    implementation(project(":clusterless-substrate-aws-common"))

    implementation("com.google.guava:guava")

    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:cloudwatch")
    implementation("software.amazon.awssdk:eventbridge")

    implementation("com.amazonaws:aws-lambda-java-core")
//    implementation("com.amazonaws:aws-lambda-java-events")
//    implementation("com.amazonaws:aws-lambda-java-serialization")
    implementation("com.amazonaws:aws-lambda-java-log4j2")

//    implementation("io.github.resilience4j:resilience4j-retry")
}

openApiGenerate {
    generatorName.set("java")
    inputSpec.set("$projectDir/src/main/json/ObjectCreated.json")
    outputDir.set(generatedRoot)
    modelPackage.set("clusterless.lambda.transform.json")

    configOptions.put("library", "native")
    configOptions.put("serializationLibrary", "jackson")
    configOptions.put("dateLibrary", "java8")

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
    dependsOn.add(tasks.openApiGenerate)
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