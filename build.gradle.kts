apply(from = "build.shared.gradle.kts")

val plugin = gradle.includedBuild("plugin")

task<Delete>("clean") {
    dependsOn(plugin.task(":clean"))
    delete(buildDir)
}

task("test") {
    dependsOn(plugin.task(":test"))
}

task("build") {
    dependsOn(plugin.task(":build"))
}

task("publish") {
    dependsOn(plugin.task(":publishPlugins"))
}
