plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.jacoco.testkit)
    alias(libs.plugins.publicationsReport)
    jacoco
}

group = "com.github.gmazzo.buildconfig"
description = "Gradle BuildConfig Plugin"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

// Stay at Java 8 to workaround the Gradle when compiling buildscripts and using our `inline` functions:
// `Cannot inline bytecode built with JVM target 11 into bytecode that is being built with JVM target 1.8` issue
// https://github.com/gmazzo/gradle-buildconfig-plugin/issues/120
java.toolchain.languageVersion = JavaLanguageVersion.of(8)

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

val pluginUnderTestDependencies by configurations.creating

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    compileOnly(gradleKotlinDsl())
    compileOnly(plugin(libs.plugins.kotlin.jvm))

    implementation(libs.javapoet)
    implementation(libs.kotlinpoet)

    testImplementation(gradleTestKit())
    testImplementation(gradleKotlinDsl())
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    pluginUnderTestDependencies(plugin(libs.plugins.kotlin.jvm))
}

gradlePlugin {
    vcsUrl.set("https://github.com/gmazzo/gradle-buildconfig-plugin")
    website.set(vcsUrl)

    plugins {
        create("buildconfig") {
            id = "com.github.gmazzo.buildconfig"
            displayName = name
            implementationClass = "com.github.gmazzo.buildconfig.BuildConfigPlugin"
            description =
                "A plugin for generating BuildConstants for any kind of Gradle projects: Java, Kotlin, Groovy, etc. Designed for KTS scripts."
            tags.addAll("buildconfig", "java", "kotlin", "kotlin-multiplatform")
        }
    }
}

tasks.withType<Test> {
    dependsOn("publishAllPublicationsToLocalRepository")
    workingDir = temporaryDir
    useJUnitPlatform()
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(libs.versions.java.get()) }
    doLast { Thread.sleep(5000) } // allows GradleRunner to store JaCoCo data before computing task outputs
}

val localRepoDir = layout.buildDirectory.dir("repo")

publishing.repositories.maven(localRepoDir) { name = "Local" }

val generateTestLocalConstants by tasks.registering {
    val localVersion = provider { project.version }
    val localRepo = layout.buildDirectory.dir("repo")
        .map { it.asFile.toRelativeString(tasks.test.get().workingDir) }

    inputs.property("version", localVersion)
    inputs.property("localRepo", localRepo)
    outputs.dir(temporaryDir)
    notCompatibleWithConfigurationCache("uses Task.project")
    doLast {
        temporaryDir.resolve("TestLocalConstants.kt").writeText(
            """
                package com.github.gmazzo.buildconfig
                
                object TestConstants {
                    const val LOCAL_VERSION = "${localVersion.get()}"
                    const val LOCAL_REPO = "${localRepo.get()}"
                }
                """.trimIndent()
        )
    }
}

kotlin.sourceSets.test {
    kotlin.srcDirs(generateTestLocalConstants)
}


tasks.pluginUnderTestMetadata {
    pluginClasspath.from(pluginUnderTestDependencies)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports.xml.required = true
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}

tasks.generateJacocoTestKitProperties {
    notCompatibleWithConfigurationCache("uses Task.extensions")
}
