import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

plugins {
    kotlin("multiplatform")
    id("com.github.gmazzo.buildconfig") version "<latest>"
}

kotlin {
    jvm()
    js { nodejs() }
}

dependencies {
    commonMainImplementation(kotlin("stdlib-common"))
    commonTestImplementation(kotlin("test-common"))
    "jvmImplementation"(kotlin("stdlib-jdk8"))
    "jvmTestImplementation"(kotlin("test-junit"))
    "jsImplementation"(kotlin("stdlib-js"))
    "jsTestImplementation"(kotlin("test-js"))
}

buildConfig {
    buildConfigField("String", "COMMON_VALUE", "\"aCommonValue\"")

    sourceSets.named<BuildConfigSourceSet>("jvmMain") {
        buildConfigField("String", "PLATFORM", "\"jvm\"")
        buildConfigField("String", "JVM_VALUE", "\"aJvmValue\"")
    }

    sourceSets.named<BuildConfigSourceSet>("jsMain") {
        buildConfigField("String", "PLATFORM", "\"js\"")
        buildConfigField("String", "JS_VALUE", "\"aJsValue\"")
    }
}

task("test") {
    dependsOn("allTests")
}
