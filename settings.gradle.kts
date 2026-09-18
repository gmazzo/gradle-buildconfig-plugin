plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "gradle/shared.settings.gradle.kts")

rootProject.name = "gradle-buildconfig-plugin"

val kmpOnly = providers.gradleProperty("kmpOnly").map(String::toBoolean).getOrElse(false)

includeBuild("plugin")
if (!kmpOnly) include(
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
)
