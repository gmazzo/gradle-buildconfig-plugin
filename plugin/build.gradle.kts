plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm")
}

apply(from = "../buildShared.gradle.kts")

base.archivesBaseName = "gradle-buildconfig-plugin"

dependencies {
    implementation(kotlin("stdlib"))
}

gradlePlugin {
    plugins {
        create("buildconfig") {
            id = "com.github.gmazzo.buildconfig"
            implementationClass = "com.github.gmazzo.gradle.plugins.BuildConfigPlugin"
        }
    }
}
