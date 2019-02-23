apply(from = "buildShared.gradle.kts")

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
