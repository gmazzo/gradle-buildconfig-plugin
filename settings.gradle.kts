enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "gradle/shared.settings.gradle.kts")

rootProject.name = "gradle-buildconfig-plugin"

val kmpOnly: String? by settings

includeBuild("plugin")
if (kmpOnly == null) include(
    "demo-project:generic",
    "demo-project:groovy",
    "demo-project:groovy-gen-kotlin",
    "demo-project:kts",
    "demo-project:kts-android",
    "demo-project:kts-android-lib",
    "demo-project:kts-gen-java",
)
include(
    "demo-project:kmp",
    "demo-project:kmp-android-legacy",
)
