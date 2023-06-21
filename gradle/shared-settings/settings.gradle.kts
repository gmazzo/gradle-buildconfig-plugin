@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
    repositories.gradlePluginPortal()
}
