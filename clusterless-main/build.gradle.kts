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

val versionBranch = project.ext["versionBranch"].toString()

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
            branch.set(versionBranch)
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

tasks.register<Exec>("generateComponentDocs") {
    dependsOn("installDist")

    workingDir = file("build/install/clusterless/bin")
    commandLine = listOf(
        "./cls",
        "show",
        "component",
        "--describe-all",
        "--output",
        "${buildDir}/generated-docs/modules/components"
    )
}

tasks.register<Exec>("generateComponentIndex") {
    dependsOn("installDist")

    workingDir = file("build/install/clusterless/bin")
    commandLine = listOf(
        "./cls",
        "show",
        "component",
        "--list",
        "--output",
        "${buildDir}/generated-docs/modules/components/"
    )
}

tasks.register<Exec>("generateComponentPartial") {
    dependsOn("installDist")

    workingDir = file("build/install/clusterless/bin")
    commandLine = listOf(
        "./cls",
        "show",
        "component",
        "--list",
        "--output",
        "${buildDir}/generated-docs/modules/components/partials",
        "--name",
        "components.adoc",
        "--template",
        "elements-list-partial-adoc"
    )
}

tasks.register<Exec>("generateModelDocs") {
    dependsOn("installDist")

    workingDir = file("build/install/clusterless/bin")
    commandLine = listOf(
        "./cls",
        "show",
        "model",
        "--describe-all",
        "--output",
        "${buildDir}/generated-docs/modules/models"
    )
}

tasks.register<Exec>("generateModelIndex") {
    dependsOn("installDist")

    workingDir = file("build/install/clusterless/bin")
    commandLine = listOf(
        "./cls",
        "show",
        "model",
        "--list",
        "--output",
        "${buildDir}/generated-docs/modules/models/"
    )
}

tasks.register<Exec>("generateModelPartial") {
    dependsOn("installDist")

    workingDir = file("build/install/clusterless/bin")
    commandLine = listOf(
        "./cls",
        "show",
        "model",
        "--list",
        "--output",
        "${buildDir}/generated-docs/modules/models/partials",
        "--name",
        "models.adoc",
        "--template",
        "elements-list-partial-adoc"
    )
}

tasks.register<Copy>("generateDocs") {
    dependsOn("generateComponentDocs")
    dependsOn("generateComponentIndex")
    dependsOn("generateComponentPartial")
    dependsOn("generateModelDocs")
    dependsOn("generateModelIndex")
    dependsOn("generateModelPartial")
    dependsOn("generateCLIDocs")
    dependsOn("generateCLIIndex")

    from("src/main/antora") {
        filter {
            it.replace("{{projectVersion}}", project.ext["versionLabel"].toString())
        }
        rename {
            it.replace(".adoc.template", ".adoc")
        }
    }
    from("${buildDir}/generated-docs/") {
        filter {
            it.lineSequence().filterNot { line ->
                line.startsWith("// ") ||
                        line.startsWith(":doctype:")
            }.joinToString("\n")
        }
    }
    into("${buildDir}/docs/")
}

val picoliExecution by configurations.creating() {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    picoliExecution("info.picocli:picocli-codegen:4.7.4")
}

tasks.register<JavaExec>("generateCLIDocs") {
    dependsOn("jar")

    classpath = picoliExecution.asFileTree
    mainClass.set("picocli.codegen.docgen.manpage.ManPageGenerator")

    args = listOf(
        "--outdir",
        "build/generated-docs/modules/commands/pages",
        "clusterless.Main"
    )
}

tasks.register("generateCLIIndex") {
    dependsOn("generateCLIDocs")

    doLast {
        val names = fileTree("build/generated-docs/modules/commands/pages")
            .map {
                it.name
            }.sortedBy { it.substringBefore(".") }
            .toList()
        println(names)
        file("build/generated-docs/modules/commands").mkdirs()
        file("build/generated-docs/modules/commands/nav.adoc")
            .writeText(
                """
.Commands
${names.joinToString("\n") { "* xref:${it}[]" }}
""".trimIndent()
            )
    }
}
