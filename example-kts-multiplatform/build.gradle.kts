import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

plugins {
    kotlin("multiplatform")
    id("com.github.gmazzo.buildconfig")
}

kotlin {
    jvm()
    js(IR) { nodejs() }
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
