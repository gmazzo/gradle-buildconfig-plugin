@file:Suppress("UnstableApiUsage")

plugins {
    base
    `maven-publish`
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.publicationsReport)
    `jacoco-report-aggregation`
}

dependencies {
    //noinspection UseTomlInstead
    jacocoAggregation("com.github.gmazzo.buildconfig:plugin")
}

val pluginBuild = gradle.includedBuild("plugin")

val jacocoTestReport by reporting.reports.creating(JacocoCoverageReport::class) {
    testSuiteName = pluginBuild.task(":test").name
}

tasks.build {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.check {
    dependsOn(jacocoTestReport.reportTask, pluginBuild.task(":$name"))
}

tasks.publish {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.publishToMavenLocal {
    dependsOn(pluginBuild.task(":$name"))
}
