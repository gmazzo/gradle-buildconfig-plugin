plugins {
    base
    alias(libs.plugins.kotlin) apply false
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

tasks.register(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.register(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}
