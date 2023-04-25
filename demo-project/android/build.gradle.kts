import com.github.gmazzo.gradle.plugins.BuildConfigTask

plugins {
    alias(libs.plugins.android)
    kotlin("android")
    id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

android {
    namespace = "com.github.gmazzo.buildconfig.demos.android"
    compileSdkVersion = "android-30"

    buildFeatures.buildConfig = true

    compileOptions {
        targetCompatibility(java.targetCompatibility)
        sourceCompatibility(java.sourceCompatibility)
    }
}

dependencies {
    testImplementation(libs.kotlin.test)
}

buildConfig {
    className("NonAndroidBuildConfig")
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")
    buildConfigField("kotlin.IntArray", "MAGIC_NUMBERS", "intArrayOf(1, 2, 3, 4)")
}

// workaround of AGP issue failing to pick test sources correctly
afterEvaluate {
    tasks.named("lintAnalyzeDebug") {
        mustRunAfter(tasks.withType<BuildConfigTask>())
    }
}
