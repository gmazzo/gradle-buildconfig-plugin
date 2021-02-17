plugins {
    kotlin("jvm") version embeddedKotlinVersion apply false
}

apply(from = "build.shared.gradle.kts")

val plugin = gradle.includedBuild("plugin")

evaluationDependsOn(":example-kts-js") // it adds some tasks to the root project

tasks.named<Delete>("clean") {
    dependsOn(plugin.task(":clean"))
    delete(buildDir)
}

task("test") {
    dependsOn(plugin.task(":test"))
}

tasks.named("build") {
    dependsOn(plugin.task(":build"))
}

task("publish") {
    dependsOn(plugin.task(":publishPlugins"))
}