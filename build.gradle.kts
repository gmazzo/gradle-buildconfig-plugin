plugins {
    base
    alias(libs.plugins.kotlin) apply false
}

val pluginBuild = gradle.includedBuild("plugin")

tasks.build {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.check {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.register(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.register(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}
