plugins {
    base
    `maven-publish`
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android) apply false
    alias(libs.plugins.publicationsReport)
    `jacoco-report-aggregation`
}

dependencies {
    jacocoAggregation("com.github.gmazzo.buildconfig:plugin")
}

val jacocoTestReport by reporting.reports.creating(JacocoCoverageReport::class) {
    testType.set(TestSuiteType.UNIT_TEST)
}

val pluginBuild = gradle.includedBuild("plugin")

tasks.build {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.check {
    dependsOn(jacocoTestReport.reportTask, pluginBuild.task(":$name"))
}

tasks.publish {
    dependsOn(pluginBuild.task(":$name"))
    finalizedBy(tasks.reportPublications)
}

tasks.publishToMavenLocal {
    dependsOn(pluginBuild.task(":$name"))
    finalizedBy(tasks.reportPublications)
}
