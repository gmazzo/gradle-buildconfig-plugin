apply(from = "buildShared.gradle.kts")

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

task("publish") {
    dependsOn(gradle.includedBuild("plugin").task(":publishPlugins"))
}