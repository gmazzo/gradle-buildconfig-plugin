pluginManagement {
    includeBuild("../gradle/shared-settings")
}
plugins {
    id("shared.settings")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
