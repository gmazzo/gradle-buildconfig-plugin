enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "gradle/shared.settings.gradle.kts")

rootProject.name = "gradle-buildconfig-plugin"

includeBuild("plugin")
include(
    "demo-project:generic",
    "demo-project:groovy",
    "demo-project:kts",
    "demo-project:kts-android",
    "demo-project:kts-multiplatform",
)
