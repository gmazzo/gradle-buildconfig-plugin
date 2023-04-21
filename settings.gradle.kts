apply(from = "gradle/shared.settings.gradle.kts")

rootProject.name = "gradle-buildconfig-plugin"

includeBuild("plugin")
include(
    "example-generic",
    "example-groovy",
    "example-kts",
    "example-kts-js",
    "example-kts-multiplatform",
)
