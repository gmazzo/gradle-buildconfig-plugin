import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import com.github.gmazzo.buildconfig.BuildConfigTask

plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.kotlin.android)
    id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())

android {
    namespace = "com.github.gmazzo.buildconfig.demos.android"
    compileSdkVersion = "android-30"

    buildFeatures.buildConfig = true

    compileOptions {
        targetCompatibility(java.targetCompatibility)
        sourceCompatibility(java.sourceCompatibility)
    }

    flavorDimensions += "brand"
    productFlavors {
        create("bar") { dimension = "brand" }
        create("foo") { dimension = "brand" }
    }

    // mimics the variant-aware buildConfigField behavior from Android, by declaring fields on the final variant sourceSet
    applicationVariants.all variant@{
        buildConfig.sourceSets.named(this@variant.name) {
            className.set("BuildConfig")

            buildConfigField("APP_NAME", project.name)
            buildConfigField("APP_SECRET", "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
            buildConfigField("BUILD_TIME", System.currentTimeMillis())
            buildConfigField("FEATURE_ENABLED", true)
            buildConfigField("MAGIC_NUMBERS", intArrayOf(1, 2, 3, 4))

            buildConfigField<Boolean>("IS_DEBUG", this@variant.buildType.isDebuggable)
            buildConfigField<String>("BRAND", this@variant.productFlavors.single().name)
        }
    }
}

dependencies {
    testImplementation(libs.kotlin.test)
}

// workaround of AGP issue failing to pick test sources correctly
tasks.withType<AndroidLintAnalysisTask>().configureEach {
    mustRunAfter(tasks.withType<BuildConfigTask>())
}
tasks.withType<LintModelWriterTask>().configureEach {
    mustRunAfter(tasks.withType<BuildConfigTask>())
}
