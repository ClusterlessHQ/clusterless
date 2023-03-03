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
    implementation(project(":clusterless-substrate-aws-lambda-common"))

    testImplementation(testFixtures(project(":clusterless-substrate-aws-lambda-common")))
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